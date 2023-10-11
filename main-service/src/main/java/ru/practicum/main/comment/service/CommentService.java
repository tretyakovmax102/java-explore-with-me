package ru.practicum.main.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.model.Comment;
import ru.practicum.main.comment.model.CommentMapper;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.State;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.request.service.RequestService;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.model.UserMapper;
import ru.practicum.main.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService  {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;
    private final RequestService requestService;

    public List<CommentDto> getComments(Integer eventId) {
        return CommentMapper.commentToDto(commentRepository.getAllByEventId(eventId));
    }

    public CommentDto createComment(Integer eventId, Integer userId, NewCommentDto inputCommentDto) {
        User user = UserMapper.userFromDto(userService.getUser(userId));
        Event event = eventService.findById(eventId);
        if (event.getState() != State.PUBLISHED)
            throw new ConflictException("event don't PUBLISHED!");

        return CommentMapper.commentToDto(
                commentRepository.save(CommentMapper.commentFromCreateDto(inputCommentDto, user, event)));
    }

    public CommentDto updateComment(Integer userId, Integer commentId, NewCommentDto inputCommentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ConflictException("comment with id = " + commentId + "not found"));
        if (!Objects.equals(comment.getCommentator().getId(), userId))
            throw  new ConflictException("someone else's comment cannot be edited!");

        comment.setText(inputCommentDto.getText());
        comment.setEdited(LocalDateTime.now());
        return CommentMapper.commentToDto(commentRepository.save(comment));
    }

    public void deleteCommentByAdmin(Integer commentId) {
        commentRepository.delete(commentRepository.findById(commentId)
                .orElseThrow(() -> new ConflictException("comment with id = " + commentId + "not found")));
    }

    public void deleteCommentByUser(Integer commentId, Integer userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ConflictException("comment with id = " + commentId + "not found"));
        if (!Objects.equals(comment.getCommentator().getId(), userId))
            throw  new ConflictException("someone else's comment cannot be edited!");

        commentRepository.delete(comment);
    }
}