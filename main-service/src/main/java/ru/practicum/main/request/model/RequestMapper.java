package ru.practicum.main.request.model;

import ru.practicum.main.request.dto.RequestDtoEvent;

import java.util.ArrayList;
import java.util.List;

public class RequestMapper {

    public static RequestDtoEvent toRequestEvent(RequestEvent requestEvent) {
        return RequestDtoEvent.builder()
                .event(requestEvent.getEvent().getId())
                .requester(requestEvent.getRequester().getId())
                .id(requestEvent.getId())
                .status(requestEvent.getStatus())
                .created(requestEvent.getCreated())
                .build();
    }

    public static List<RequestDtoEvent> toRequestDtoEvents(List<RequestEvent> requestEvents) {
        List<RequestDtoEvent> requestDtoEvents = new ArrayList<RequestDtoEvent>();
        for (RequestEvent requestEvent : requestEvents) {
            requestDtoEvents.add(toRequestEvent(requestEvent));
        }
        return requestDtoEvents;
    }
}
