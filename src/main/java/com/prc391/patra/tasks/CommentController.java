package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tasks/{id}/comments")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity comment(@PathVariable("id") String id, @RequestBody Comment comment) throws EntityNotFoundException {
        boolean result = commentService.comment(id, comment);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PutMapping("/{commentId}")
    public ResponseEntity editComment(@PathVariable("id") String taskId, @PathVariable("commentId") String commentId,
                                           @RequestBody Comment comment) throws EntityNotFoundException {
        boolean result = commentService.updateComment(taskId, commentId, comment);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
