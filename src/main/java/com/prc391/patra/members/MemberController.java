package com.prc391.patra.members;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.members.requests.CreateMemberRequest;
import com.prc391.patra.orgs.Organization;
import com.prc391.patra.orgs.requests.CreateOrganizationRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v0/members")
public class MemberController {

    private final MemberService memberService;
    private final ModelMapper mapper;

    @Autowired
    public MemberController(MemberService memberService, ModelMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }

    @GetMapping("/")
    public ResponseEntity<List<Member>> getMultiMember(
            @RequestParam List<String> memberIDs
    ) throws EntityNotFoundException {
        return ResponseEntity.ok(memberService.getMultiMember(memberIDs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(
            @PathVariable("id") String id
    ) throws EntityNotFoundException {
        return ResponseEntity.ok(memberService.getMember(id));
    }

    @PostMapping
    public ResponseEntity<Member> insertMember(
            @RequestBody CreateMemberRequest newMember) throws EntityNotFoundException {
        return ResponseEntity.ok(memberService.insertMember(mapper.map(newMember,Member.class)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(
            @PathVariable("id") String id,
            @RequestBody CreateMemberRequest updateMember) throws EntityNotFoundException {
        return ResponseEntity.ok(memberService.updateMember(id, mapper.map(updateMember, Member.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteMember(
            @PathVariable("id") String id
    ) throws EntityNotFoundException {
        memberService.deleteMember(id);
        return ResponseEntity.ok().build();
    }
}
