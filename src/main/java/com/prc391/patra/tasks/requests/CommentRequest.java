package com.prc391.patra.tasks.requests;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CommentRequest {
    @NotEmpty
    private String memberId;
    @NotEmpty
    private String comment;
}
