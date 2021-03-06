package com.prc391.patra.members;

import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.responses.MemberResponse;
import com.prc391.patra.orgs.Organization;
import com.prc391.patra.orgs.OrganizationRepository;
import com.prc391.patra.tasks.TaskRepository;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRedis;
import com.prc391.patra.users.UserRedisRepository;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuthorizationUtils authorizationUtils;
    private final UserRedisRepository userRedisRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper mapper;

    public Member getMember(String memberId) throws EntityNotFoundException {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (!optionalMember.isPresent()) {
            throw new EntityNotFoundException();
        }

        return optionalMember.get();
    }

    public List<MemberResponse> getMultiMember(List<String> memberIds) throws EntityNotFoundException {
        List<Member> optionalMember = (List<Member>) memberRepository.findAllById(memberIds);
        if (CollectionUtils.isEmpty((optionalMember))) {
            throw new EntityNotFoundException();
        }
        List<MemberResponse> memberResponseList = new ArrayList<>();
        for (Member member : optionalMember) {
            Optional<User> optionalUser = userRepository.findById(member.getUsername());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                MemberResponse memberResponse = mapper.map(member, MemberResponse.class);
                memberResponse.setFullName(user.getName());
                memberResponseList.add(memberResponse);
                Optional<Organization> optionalOrganization = organizationRepository.findById(member.getOrgId());
                memberResponse.setOrganization(optionalOrganization.get());
            }
        }
        return memberResponseList;
    }

    public Member insertMember(Member newMember) throws EntityNotFoundException, EntityExistedException, UnauthorizedException {
        if (ObjectUtils.isEmpty(newMember.getOrgId())
                || ObjectUtils.isEmpty(newMember.getUsername())) {
//                || PatraStringUtils.isBlankAndEmpty(newMember.getPermission()))

            throw new EntityNotFoundException("insertMember: required fields (OrgId, username, permissions) not found!");
        }
        Member memInDB = memberRepository.getByUsernameAndOrgId(newMember.getUsername(), newMember.getOrgId());
        if (!ObjectUtils.isEmpty(memInDB)) {
            throw new EntityExistedException("Member " + memInDB.getMemberId() + " is exist");
        }
//        if (!authorizationUtils.authorizeAccess(newMember.getOrgId(), SecurityConstants.ADMIN_ACCESS)) {
//            throw new UnauthorizedException("You don't have permission to access this resource");
//        }
        validateMember(newMember);
        newMember.setPermission(SecurityConstants.READ_ACCESS);
        Member newMemSavedInDB = memberRepository.save(newMember);
        updateUserInRedis(newMemSavedInDB.getUsername());
        return newMemSavedInDB;
    }

    public Member updateMember(String id, Member updateMember) throws EntityNotFoundException, UnauthorizedException {
        Optional<Member> optionalCurrMember = memberRepository.findById(id);
        if (!optionalCurrMember.isPresent()) {
            throw new EntityNotFoundException();
        }
        validateMember(updateMember);

        Member currMember = optionalCurrMember.get();
        if (!authorizationUtils.authorizeAccess(currMember.getOrgId(), SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        currMember.setPermission(updateMember.getPermission());
        Member updatedMem = memberRepository.save(currMember);
        updateUserInRedis(updatedMem.getUsername());
        return updatedMem;
    }

    public void deleteMember(String id) throws EntityNotFoundException, UnauthorizedException {
        Optional<Member> optionalCurrMember = memberRepository.findById(id);
        if (!optionalCurrMember.isPresent()) {
            throw new EntityNotFoundException();
        }
        Member deletingMember = optionalCurrMember.get();
        //check if user is the org creator, if yes, then this member cannot be deleted.
        //WHen the org is deleted, the orgCreator member would be deleted
        Organization organization = organizationRepository.findById(deletingMember.getOrgId()).get();
        if (deletingMember.getMemberId().equalsIgnoreCase(organization.getOrgCreator())) {
            throw new UnauthorizedException("You cannot remove the organization creator. " +
                    "You must delete the organization if you want to do that");
        }
        if (!authorizationUtils.authorizeAccess(deletingMember.getOrgId(), SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        String username = deletingMember.getUsername();
        List<String> assignedTaskIds = deletingMember.getAssignedTaskId();
        taskRepository.removeAssigneeInMultipleTask(assignedTaskIds, Arrays.asList(deletingMember.getMemberId()));
        memberRepository.deleteById(id);
        updateUserInRedis(username);
    }

    /**
     * Validate Member before proceeding to next action
     * Checks whether Organization, User, Permission exists
     * Only validate field if the field is provided
     *
     * @param member member to check
     * @throws EntityNotFoundException when any Organization, or User, or Permission not exist
     */
    private void validateMember(Member member) throws EntityNotFoundException {
        if (!ObjectUtils.isEmpty(member)) {
            //only check if the Organization id is provided. Skip if not provided
            if (!ObjectUtils.isEmpty(member.getOrgId())) {
                Optional<Organization> optionalOrganization = organizationRepository.findById(member.getOrgId());
                if (!optionalOrganization.isPresent()) {
                    throw new EntityNotFoundException("Organization with id " + member.getOrgId() + " not exist!");
                }
            }
            //only check if the username is provided. Skip if not provided
            if (!ObjectUtils.isEmpty(member.getUsername())) {
                Optional<User> optionalUser = userRepository.findById(member.getUsername());
                if (!optionalUser.isPresent()) {
                    throw new EntityNotFoundException("User with username " + member.getUsername() + " not exist!");
                }
            }
        }
    }

    private void updateUserInRedis(String username) {
        userRedisRepository.deleteById(username);
        UserRedis userInRedis = mapper.map(userRepository.findById(username).get(), UserRedis.class);
        Map<String, String> orgPermissions = memberRepository.getAllByUsername(username).stream()
                .collect(Collectors.toMap(Member::getOrgId, Member::getPermission));
        userInRedis.setOrgPermissions(orgPermissions);
        userRedisRepository.save(userInRedis);
    }
}
