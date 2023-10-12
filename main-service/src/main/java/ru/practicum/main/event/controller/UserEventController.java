package ru.practicum.main.event.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.InputCommentDto;
import ru.practicum.main.comment.service.CommentService;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.request.dto.RequestDtoEvent;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventController {

    EventService eventService;
    CommentService commentService;

    @GetMapping("{eventId}")
    public EventDto getEvent(@PathVariable Integer userId, @PathVariable Integer eventId) {
        return eventService.getEvent(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getEvents(
            @PathVariable Integer userId,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size
    ) {
        return eventService.getEvents(userId, from, size);
    }

    @GetMapping("{eventId}/requests")
    public List<RequestDtoEvent> getEventRequests(@PathVariable Integer userId, @PathVariable Integer eventId) {
        return eventService.getEventRequests(userId, eventId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@PathVariable Integer userId, @Valid @RequestBody CreateEventDto createEventDto) {
        return eventService.addEvent(userId, createEventDto);
    }

    @PatchMapping("{eventId}")
    public EventDto patchEvent(
            @PathVariable Integer userId,
            @PathVariable Integer eventId,
            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return eventService.changeEvent(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("{eventId}/requests")
    public EventRequestStatusUpdateResult patchEventRequests(
            @PathVariable Integer userId,
            @PathVariable Integer eventId,
            @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return eventService.changeEventRequests(userId, eventId, eventRequestStatusUpdateRequest);
    }

    @PostMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Integer eventId, @PathVariable Integer userId,
                                    @RequestBody @Valid InputCommentDto commentInputDto) {
        return commentService.createComment(eventId, userId, commentInputDto);
    }

    @PatchMapping("/{eventId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable Integer userId, @PathVariable Integer commentId,
                                    @RequestBody @Valid InputCommentDto commentInputDto) {
        return commentService.updateComment(userId, commentId, commentInputDto);
    }

    @DeleteMapping("/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Integer commentId, @PathVariable Integer userId) {
        commentService.deleteCommentByUser(commentId, userId);
    }
}
