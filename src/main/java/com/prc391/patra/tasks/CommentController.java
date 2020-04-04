package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.tasks.requests.CommentRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v0/tasks/{id}/comments")
public class CommentController {
    private final CommentService commentService;
    private final ModelMapper mapper;

    @Autowired
    public CommentController(CommentService commentService, ModelMapper mapper) {
        this.commentService = commentService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity comment(@PathVariable("id") String id,
                                  @Valid @RequestBody CommentRequest commentContent)
            throws EntityNotFoundException, UnauthorizedException {
        boolean result = commentService.comment(id, commentContent.getComment());
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PutMapping("/{commentId}")
    public ResponseEntity editComment(
            @PathVariable("id") String taskId,
            @PathVariable("commentId") String commentId,
            @Valid @RequestBody String request) throws EntityNotFoundException, UnauthorizedException {
        boolean result = commentService.updateComment(taskId, commentId, request);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity deleteComment(@PathVariable("id") String taskId, @PathVariable("commentId") String commentId) throws EntityNotFoundException, UnauthorizedException {
        boolean result = commentService.deleteComment(taskId, commentId);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
