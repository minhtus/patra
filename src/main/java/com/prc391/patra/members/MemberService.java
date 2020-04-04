package com.prc391.patra.members;

import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.orgs.Organization;
import com.prc391.patra.orgs.OrganizationRepository;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import com.prc391.patra.utils.PatraStringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuthorizationUtils authorizationUtils;

    public Member getMember(String memberId) throws EntityNotFoundException {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (!optionalMember.isPresent()) {
            throw new EntityNotFoundException();
        }

        return optionalMember.get();
    }

    public List<Member> getMultiMember(List<String> memberIds) throws EntityNotFoundException {
        List<Member> optionalMember = (List<Member>) memberRepository.findAllById(memberIds);
        if (CollectionUtils.isEmpty((optionalMember))) {
            throw new EntityNotFoundException();
        }

        return optionalMember;
    }

    public Member insertMember(Member newMember) throws EntityNotFoundException, EntityExistedException {
        if (ObjectUtils.isEmpty(newMember.getOrgId())
        || ObjectUtils.isEmpty(newMember.getUsername())
        || PatraStringUtils.isBlankAndEmpty(newMember.getPermission())) {
            throw new EntityNotFoundException("insertMember: required fields (OrgId, username, permissions) not found!");
        }
        Member memInDB = memberRepository.getByUsernameAndOrgId(newMember.getOrgId(), newMember.getUsername());
        if (!ObjectUtils.isEmpty(memInDB)) {
            throw new EntityExistedException("Member " + memInDB.getMemberId() + " is exist");
        }
        validateMember(newMember);
        return memberRepository.save(newMember);
    }

    public Member updateMember(String id, Member updateMember) throws EntityNotFoundException {
        Optional<Member> optionalCurrMember = memberRepository.findById(id);
        if (!optionalCurrMember.isPresent()) {
            throw new EntityNotFoundException();
        }
        validateMember(updateMember);

        Member currMember = optionalCurrMember.get();
        currMember.mergeToUpdate(updateMember);
        return memberRepository.save(currMember);
    }

    public void deleteMember(String id) throws EntityNotFoundException {
        Optional<Member> optionalCurrMember = memberRepository.findById(id);
        if (!optionalCurrMember.isPresent()) {
            throw new EntityNotFoundException();
        }
        memberRepository.deleteById(id);
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
}
