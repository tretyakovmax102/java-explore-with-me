package ru.practicum.main.request.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.request.dto.RequestDtoEvent;
import ru.practicum.main.request.service.RequestService;

import java.util.List;

@Validated
@RestController
@AllArgsConstructor
public class RequestController {

    private final RequestService requestsService;

    @GetMapping("/users/{userId}/requests")
    public List<RequestDtoEvent> getRequests(@PathVariable int userId) {
        return requestsService.getRequests(userId);
    }

    @PostMapping(value = "/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDtoEvent addRequest(@PathVariable int userId, @RequestParam int eventId) {
        return requestsService.addRequest(userId, eventId);
    }

    @PatchMapping(value = "/users/{userId}/requests/{requestId}/cancel")
    public RequestDtoEvent cancelRequest(@PathVariable int userId, @PathVariable int requestId) {
        return requestsService.cancelRequest(userId, requestId);
    }
}