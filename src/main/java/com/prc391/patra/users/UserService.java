package com.prc391.patra.users;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.InvalidInputException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.jwt.JwtRedisRepository;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.members.responses.MemberResponse;
import com.prc391.patra.orgs.Organization;
import com.prc391.patra.orgs.OrganizationRepository;
import com.prc391.patra.users.requests.ChangePassRequest;
import com.prc391.patra.utils.ControllerSupportUtils;
import com.prc391.patra.utils.PatraStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
//    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRedisRepository userRedisRepository;
    private final ModelMapper mapper;
    private final JwtRedisRepository jwtRedisRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MemberRepository memberRepository, OrganizationRepository organizationRepository, UserRedisRepository userRedisRepository, ModelMapper mapper, JwtRedisRepository jwtRedisRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
//        this.roleRepository = roleRepository;
        this.memberRepository = memberRepository;
        this.organizationRepository = organizationRepository;
        this.userRedisRepository = userRedisRepository;
        this.mapper = mapper;
        this.jwtRedisRepository = jwtRedisRepository;
    }

    public User registerUser(User newUserInfo) throws EntityExistedException {
        if (ObjectUtils.isEmpty(newUserInfo)) {
            //throw exception here (if necessary)
            return null;
        }
        Optional<User> userInDB = userRepository.findById(newUserInfo.getUsername());
        if (userInDB.isPresent()) {
            throw new EntityExistedException("User " + newUserInfo.getUsername() + " is existed!");
        }
        newUserInfo.setPassHash(passwordEncoder.encode(newUserInfo.getPassHash()));
        newUserInfo.setEnabled(true);
        //TODO: implement email verification, if possible
//        user.setEmail(newUserInfo.getEmail());

//        List<Long> userRoles = new ArrayList<>();
//        for (Long roleId : newUserInfo.getRoles()) {
//            Optional<Role> currentRole = roleRepository.findById(roleId);
//            if (currentRole.isPresent()) {
//                userRoles.add(roleId);
//            } else {
//                //TODO: role is not present
//            }
//        }
//        user.setRoles(userRoles);
        return userRepository.save(newUserInfo);
    }

    public User getUser(String username) throws EntityNotFoundException {
        if (StringUtils.isEmpty(username)) {
            Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principalObject instanceof PatraUserPrincipal)) {
                throw new EntityNotFoundException("Something wrong with Principal");
            }
            PatraUserPrincipal principal = (PatraUserPrincipal) principalObject;
            username = principal.getUsername();
        }
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent()) {
            throw new EntityNotFoundException("User " + username + " not found");
        }
        return user.get();
    }

    public List<Organization> getUserOrganization(String username) throws EntityNotFoundException {
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent()) {
            throw new EntityNotFoundException("User " + username + " not found");
        }
        List<Member> memberList = memberRepository.getAllByUsername(username);
        List<String> orgIdList = memberList.stream()
                .map(member -> member.getOrgId())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orgIdList)) {
            throw new EntityNotFoundException("OrgIds is null");
        }
        List<Organization> organizationList = organizationRepository.getAllByOrgIdIn(orgIdList);
        if (CollectionUtils.isEmpty(organizationList)) {
            throw new EntityNotFoundException("Org not exist!");
        }
        return organizationList;
    }

    public List<MemberResponse> getUserMember(String username) throws EntityNotFoundException {
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent()) {
            throw new EntityNotFoundException("User " + username + " not found");
        }
        List<Member> memberList = memberRepository.getAllByUsername(username);
        List<MemberResponse> memberResponses = new ArrayList<>();
        for (Member member : memberList) {
            MemberResponse memberResponse = mapper.map(member, MemberResponse.class);
            Optional<Organization> optionalOrganization = organizationRepository.findById(member.getOrgId());
            if (optionalOrganization.isPresent()) {
                memberResponse.setOrganization(optionalOrganization.get());
            }
            memberResponses.add(memberResponse);
        }
        return memberResponses;
    }

    public void updateCurrMemberId(String username, String currMemberId) throws EntityNotFoundException, InvalidInputException {
        Optional<User> optionalUser = userRepository.findById(username);
        if (!optionalUser.isPresent()) {
            throw new EntityNotFoundException("User " + username + " not found");
        }
        User user = optionalUser.get();
        if (user.getCurrMemberId().equalsIgnoreCase(currMemberId)) {
            throw new InvalidInputException("New currMemId is the same as old currMemId!");
        }
        Optional<Member> optionalMember = memberRepository.findById(currMemberId);
        if (!optionalMember.isPresent()) {
            throw new EntityNotFoundException("Member " + currMemberId + " is not exist");
        }
        user.setCurrMemberId(currMemberId);
        //change curr-member-id, overall memberIds of user is not changed
        //but if the memberIds is not presisted, it will lost
        updateUserInRedis(user);
    }

    public boolean changePassword(ChangePassRequest changePassRequest) throws UnauthorizedException, EntityNotFoundException {
        if (ObjectUtils.isEmpty(changePassRequest)) {
            throw new EntityNotFoundException("ChangePassRequest is empty");
        }
        if (PatraStringUtils.isBlankAndEmpty(changePassRequest.getNewPassword()) ||
                PatraStringUtils.isBlankAndEmpty(changePassRequest.getOldPassword())) {
            throw new EntityNotFoundException("Old pass or New pass is empty");
        }
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        if (ObjectUtils.isEmpty(principal)) {
            throw new UnauthorizedException("Unauthorized");
        }
        Optional<User> optionalUser = userRepository.findById(principal.getUsername());
        if (!optionalUser.isPresent()) {
            throw new EntityNotFoundException("User " + principal.getUsername() + " not exist in db");
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(changePassRequest.getOldPassword(), user.getPassHash())) {
            throw new UnauthorizedException("Old password is not match");
        }
        user.setPassHash(passwordEncoder.encode(changePassRequest.getNewPassword()));
        userRepository.save(user);
        updateUserInRedis(user);

        return true;
    }

    private void updateUserInRedis(User user) {
        userRedisRepository.deleteById(user.getUsername());
        UserRedis userRedis = mapper.map(user, UserRedis.class);
        List<Member> memberList = memberRepository.getAllByUsername(user.getUsername());
        userRedis.setMemberIds(memberList.stream().map(member -> member.getMemberId()).collect(Collectors.toList()));
        userRedisRepository.save(userRedis);
        userRepository.save(user);
    }
}
