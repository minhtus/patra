package com.prc391.patra.members;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MemberRepository extends MongoRepository<Member, String>, MemberRepositoryCustom {
    List<Member> getAllByUsername(String username);
}
