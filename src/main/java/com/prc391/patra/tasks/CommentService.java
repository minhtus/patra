package com.prc391.patra.tasks;

import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.sheets.Sheet;
import com.prc391.patra.sheets.SheetRepository;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import com.prc391.patra.utils.ControllerSupportUtils;
import com.prc391.patra.utils.PatraStringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CommentService {
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final SheetRepository sheetRepository;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public CommentService(TaskRepository taskRepository, MemberRepository memberRepository, UserRepository userRepository, SheetRepository sheetRepository, AuthorizationUtils authorizationUtils) {
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.sheetRepository = sheetRepository;
        this.authorizationUtils = authorizationUtils;
    }

    Map<String, String> comment(String taskId, String commentContent) throws EntityNotFoundException, UnauthorizedException {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new EntityNotFoundException("Task not exist");
        }
        Task task = optionalTask.get();
        Sheet sheet = sheetRepository.findById(task.getSheetId()).get();
        String memberId = memberRepository.getByUsernameAndOrgId(ControllerSupportUtils.getPatraPrincipal().getUsername(), sheet.getOrgId()).getMemberId();
//        if (PatraStringUtils.isBlankAndEmpty(memberId)) {
//            throw new EntityNotFoundException("MemberId is null");
//        }
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (!optionalMember.isPresent()) {
            throw new EntityNotFoundException("Member " + memberId + " is not exist");
        }
        Member member = optionalMember.get();
        isAuthorized(task, member);
        Comment comment = new Comment();
        comment.setCommentId(new ObjectId().toString());
        comment.setMemberId(memberId);
        String username = optionalMember.get().getUsername();
        User user = userRepository.findById(username).get();
        comment.setUsername(user.getName());
        comment.setComment(commentContent);
        taskRepository.insertComment(taskId, comment);
        Map<String, String> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("commentId", comment.getCommentId());
        return result;
    }

    boolean updateComment(String taskId, String commentId, String commentContent) throws EntityNotFoundException, UnauthorizedException {
        if (taskRepository.existsById(taskId) && taskRepository.commentExist(taskId, commentId)) {
            Task task = taskRepository.findById(taskId).get();
            Comment comment = task.getComments().stream()
                    .filter(commentInTask -> commentInTask.getCommentId().equalsIgnoreCase(commentId))
                    .findFirst().get();
            String memberId = comment.getMemberId();
            if (PatraStringUtils.isBlankAndEmpty(memberId)) {
                throw new EntityNotFoundException("MemberId is null");
            }
            Optional<Member> optionalMember = memberRepository.findById(memberId);
            if (!optionalMember.isPresent()) {
                throw new EntityNotFoundException("Member " + memberId + " is not exist");
            }
            Member member = optionalMember.get();
            isAuthorized(task, member);
            //check if editing user is the original commentor (ex: tantk cannot change phuongdt's comment)
            String currentUsername = ControllerSupportUtils.getPatraPrincipal().getUsername();
            if (!member.getUsername().equalsIgnoreCase(currentUsername)) {
                throw new UnauthorizedException("You don't have permission to access this resource");
            }
            comment.setCommentId(commentId);
            comment.setComment(commentContent);
            return taskRepository.updateComment(taskId, comment);
        } else {
            throw new EntityNotFoundException();
        }
    }

    boolean deleteComment(String taskId, String commentId) throws EntityNotFoundException, UnauthorizedException {
        if (taskRepository.existsById(taskId) && taskRepository.commentExist(taskId, commentId)) {
            Task task = taskRepository.findById(taskId).get();
            Comment comment  = task.getComments().stream()
                    .filter(commentInTask -> commentInTask.getCommentId().equalsIgnoreCase(commentId))
                    .findFirst().get();
            String memberId = comment.getMemberId();
            if (PatraStringUtils.isBlankAndEmpty(memberId)) {
                throw new EntityNotFoundException("MemberId is null");
            }
            Optional<Member> optionalMember = memberRepository.findById(memberId);
            if (!optionalMember.isPresent()) {
                throw new EntityNotFoundException("Member " + memberId + " is not exist");
            }
            Member member = optionalMember.get();
            isAuthorized(task, member);
            //check if editing user is the original commentor (ex: tantk cannot change phuongdt's comment)
            String currentUsername = ControllerSupportUtils.getPatraPrincipal().getUsername();
            if (!member.getUsername().equalsIgnoreCase(currentUsername)) {
                throw new UnauthorizedException("You don't have permission to access this resource");
            }
            return taskRepository.deleteComment(taskId, commentId);
        } else {
            throw new EntityNotFoundException();
        }
    }

    private void isAuthorized(Task task, Member member) throws EntityNotFoundException, UnauthorizedException {
        Optional<Sheet> optionalSheet = sheetRepository.findById(task.getSheetId());
        if (!optionalSheet.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + task.getSheetId() + " not exist!");
        }
        Sheet sheet = optionalSheet.get();
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.WRITE_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        if (!task.getAssignee().contains(member.getMemberId())) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
    }
}
