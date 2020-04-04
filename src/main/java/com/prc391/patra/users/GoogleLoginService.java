package com.prc391.patra.users;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.utils.PatraStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Service
public class GoogleLoginService {
    private final UserRepository userRepository;

    private final JsonFactory jsonFactory = new JacksonFactory();

    public GoogleLoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerNewGoogleUser(String googleIdToken) throws UnauthorizedException {
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
            user.setEmail(email);
            user.setImageUrl(imageUrl);
            userRepository.save(user);
            return user;
        }
        return null;
    }

    public boolean checkUserExisted(String googleIdToken) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(SecurityConstants.GOOGLE_CLIENT_ID))
                .build();

        GoogleIdToken idToken = null;
        try {
            if (StringUtils.isEmpty(googleIdToken)) {
                return false;
            }
            idToken = verifier.verify(googleIdToken);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!ObjectUtils.isEmpty(idToken)) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String id = payload.getSubject();
            if (!checkGoogleUserExisted(id)) {
                return false;
            }
            return true;
        } else {
            System.out.println("Invalid ID Token");
            return false;
        }
    }

    private boolean checkGoogleUserExisted(String googleUserId) {
        Optional<User> optionalUser = userRepository.findById(googleUserId);
        if (optionalUser.isPresent()) {
            return true;
        }
        return false;
    }
}
