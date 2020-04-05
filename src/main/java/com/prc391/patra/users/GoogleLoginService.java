package com.prc391.patra.users;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.utils.PatraStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GoogleLoginService {
    private final UserRepository userRepository;
    private final UserRedisRepository userRedisRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper mapper;

    private final JsonFactory jsonFactory = new JacksonFactory();

    public GoogleLoginService(UserRepository userRepository, UserRedisRepository userRedisRepository, MemberRepository memberRepository, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.userRedisRepository = userRedisRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
    }

    public User googleLogin(String googleIdToken) throws UnauthorizedException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(SecurityConstants.GOOGLE_CLIENT_ID)).build();
        GoogleIdToken idToken = null;
        try {
            if (PatraStringUtils.isEmpty(googleIdToken)) {
                throw new UnauthorizedException("Empty ID Token");
            }
            idToken = verifier.verify(googleIdToken);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ObjectUtils.isEmpty(idToken)) { //invalid  id token
            throw new UnauthorizedException("Invalid ID Token");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String id = payload.getSubject();
        if (!checkGoogleUserExisted(id)) {
            String email = payload.getEmail();
//                boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String imageUrl = (String) payload.get("picture");

            User user = new User();
            user.setUsername(id);
            user.setName(name);
            user.setEnabled(true);
            user.setEmail(email);
            user.setImageUrl(imageUrl);
            userRepository.save(user);
            updateUserInRedis(user);
            return user;
        } else {
            User user = userRepository.findById(id).get();
            updateUserInRedis(user);
            return user;
        }
    }

    private boolean checkGoogleUserExisted(String googleUserId) {
        Optional<User> optionalUser = userRepository.findById(googleUserId);
        if (optionalUser.isPresent()) {
            return true;
        }
        return false;
    }

    private void updateUserInRedis(User user) {
        userRedisRepository.deleteById(user.getUsername());
        UserRedis userRedis = mapper.map(user, UserRedis.class);
        Map<String, String> orgPermission = memberRepository.getAllByUsername(user.getUsername()).stream()
                .collect(Collectors.toMap(Member::getOrgId, Member::getPermission));
        userRedis.setOrgPermissions(orgPermission);
        userRedisRepository.save(userRedis);
    }
}
