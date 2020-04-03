package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.utils.PatraStringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(TaskRepository taskRepository, MemberRepository memberRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    boolean comment(String taskId, Comment comment) throws EntityNotFoundException {
        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException();
        }
        comment.setCommentId(new ObjectId().toString());
        String memberId = comment.getMemberId();
        if (PatraStringUtils.isBlankAndEmpty(memberId)) {
            throw new EntityNotFoundException("MemberId is null");
        }
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (!optionalMember.isPresent()) {
            throw new EntityNotFoundException("Member " + memberId + " is not exist");
        }
        String username = optionalMember.get().getUsername();
        User user = userRepository.findById(username).get();
        comment.setUsername(user.getName());
        return taskRepository.insertComment(taskId, comment);
    }

    boolean updateComment(String taskId, String commentId, Comment comment) throws EntityNotFoundException {
        if (taskRepository.existsById(taskId) && taskRepository.commentExist(taskId, commentId)) {
            comment.setCommentId(commentId);
            return taskRepository.updateComment(taskId, comment);
        } else {
            throw new EntityNotFoundException();
        }
    }

    boolean deleteComment(String taskId, String commentId) throws EntityNotFoundException {
        if (taskRepository.existsById(taskId) && taskRepository.commentExist(taskId, commentId)) {
            return taskRepository.deleteComment(taskId, commentId);
        } else {
            throw new EntityNotFoundException();
        }
    }
}
