package com.prc391.patra.tasks.requests;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CommentRequest {
    @NotEmpty
    private String username;
    @NotEmpty
    private String comment;
}
