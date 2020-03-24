package com.prc391.patra.users;

import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.orgs.Organization;
import com.prc391.patra.orgs.OrganizationRepository;
import com.prc391.patra.users.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, MemberRepository memberRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.memberRepository = memberRepository;
        this.organizationRepository = organizationRepository;
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
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent()) {
            throw new EntityNotFoundException("User "+ username +" not found");
        }
        return user.get();
    }

    public List<Organization> getUserOrganization(String username) throws EntityNotFoundException {
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent()) {
            throw new EntityNotFoundException("User "+ username +" not found");
        }
        List<Member> memberList = memberRepository.getAllByUsername(username);
        List<String> orgIdList = memberList.stream()
                .map(member -> member.getOrgId())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orgIdList)) {
            throw new EntityNotFoundException("OrgIds is null");
        }
        List<Organization> organizationList = organizationRepository.getAllByIdIn(orgIdList);
        if (CollectionUtils.isEmpty(organizationList)) {
            throw new EntityNotFoundException("Org not exist!");
        }
        return organizationList;
    }

}
