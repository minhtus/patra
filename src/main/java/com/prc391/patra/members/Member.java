package com.prc391.patra.members;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
public class Member {
    @MongoId
    @Field("member_id")
    private String memberId;
}
