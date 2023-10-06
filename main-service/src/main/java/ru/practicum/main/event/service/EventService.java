package ru.practicum.main.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.client.Client;
import ru.practicum.dto.HitDto;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventMapper;
import ru.practicum.main.event.model.State;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.ForbiddenException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.request.dto.RequestDtoEvent;
import ru.practicum.main.request.model.RequestEvent;
import ru.practicum.main.request.model.RequestMapper;
import ru.practicum.main.request.model.Status;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.model.UserMapper;
import ru.practicum.main.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ru.practicum.main.event.model.State.PENDING;
import static ru.practicum.main.event.model.State.PUBLISHED;
import static ru.practicum.main.event.model.StateActionAdmin.PUBLISH_EVENT;
import static ru.practicum.main.event.model.StateActionAdmin.REJECT_EVENT;
import static ru.practicum.main.event.model.StateActionUser.CANCEL_REVIEW;
import static ru.practicum.main.event.model.StateActionUser.SEND_TO_REVIEW;
import static ru.practicum.main.request.model.Status.CONFIRMED;
import static ru.practicum.main.request.model.Status.REJECTED;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventService {

    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    RequestRepository requestRepository;
    UserService userService;
    EventMapper eventMapper;
    Client statsClient;

    public List<EventShortDto> getEvents(Integer userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findAllByInitiator_Id(userId, PageRequest.of(from, size))
                .getContent();
        return eventMapper.toEventDtos(events);
    }

    public EventDto addEvent(Integer userId, CreateEventDto createEventDto) {
        if (LocalDateTime.now().plusHours(1).isAfter(createEventDto.getEventDate())) {
            throw new ForbiddenException("date error");
        }
        User user = UserMapper.userFromDto(userService.getUser(userId));
        Event toSave = eventMapper.toEvent(createEventDto);
        if (createEventDto.getCategory() != null) {
            Category category = categoryRepository.findById(createEventDto.getCategory())
                    .orElseThrow(NotFoundException::new);
            toSave.setCategory(category);
        }
        toSave.setInitiator(user);
        toSave.setCreatedOn(LocalDateTime.now());
        toSave.setState(PENDING);
        toSave.setConfirmedRequests(0);
        if (createEventDto.getPaid() == null) toSave.setPaid(false);
        if (createEventDto.getParticipantLimit() == null) toSave.setParticipantLimit(0);
        if (createEventDto.getRequestModeration() == null) toSave.setRequestModeration(true);
        eventRepository.save(toSave);
        return eventMapper.toEventFullDto(toSave);
    }

    public EventDto getEvent(Integer userId, Integer eventId) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId).orElseThrow(NotFoundException::new);
        return eventMapper.toEventFullDto(event);
    }

    public EventDto changeEvent(Integer userId, Integer eventId, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getEventDate() != null) {
            if (LocalDateTime.now().plusHours(1).isAfter(updateEventUserRequest.getEventDate())) {
                throw new ForbiddenException("date error");
            }
        }
        Event event = eventRepository.findById(eventId).orElseThrow(NotFoundException::new);
        if (event.getState() == PUBLISHED) {
            throw new ConflictException("state = published");
        }
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("player isnt initiator");
        }
        eventMapper.updateEvent(event, updateEventUserRequest);
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(NotFoundException::new);
            event.setCategory(category);
        }
        if (updateEventUserRequest.getStateAction() == SEND_TO_REVIEW)
            event.setState(PENDING);
        if (updateEventUserRequest.getStateAction() == CANCEL_REVIEW)
            event.setState(State.CANCELED);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(savedEvent);
    }

    public List<RequestDtoEvent> getEventRequests(Integer userId, Integer eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(NotFoundException::new);
        List<RequestEvent> requests;
        if (event.getInitiator().getId().equals(userId)) {
            requests = requestRepository.findAllByEvent_Id(eventId);
        } else {
            requests = requestRepository.findAllByRequester_IdAndEvent_Id(userId, eventId);
        }
        return toParticipationRequestDtos(requests);
    }

    public EventRequestStatusUpdateResult changeEventRequests(
            Integer userId,
            Integer eventId,
            EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId).orElseThrow(() -> new NotFoundException("event not found"));
        if (event.getConfirmedRequests() != null && event.getParticipantLimit() == 0 || !event.getRequestModeration())
            throw new BadRequestException("eventRequest cannot be patched");

        if (requestRepository.existsParticipationRequestByIdInAndStatus(eventRequestStatusUpdateRequest.getRequestIds(), CONFIRMED))
            throw new ConflictException("event status is PENDING!");

        if (event.getConfirmedRequests() != null && event.getConfirmedRequests().equals(event.getParticipantLimit()))
            throw new ConflictException("eventRequest cannot be patched");

        List<RequestEvent> requests = requestRepository.findAllById(eventRequestStatusUpdateRequest.getRequestIds());
        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = new EventRequestStatusUpdateResult();
        if (eventRequestStatusUpdateRequest.getStatus() == REJECTED) {
            List<RequestDtoEvent> requestDtoEvents = setRequestStatus(requests, REJECTED);
            eventRequestStatusUpdateResult.setRejectedRequests(requestDtoEvents);
            event.setConfirmedRequests(event.getConfirmedRequests() - requests.size());
        } else {
            List<RequestDtoEvent> requestDtoEvents = setRequestStatus(requests, CONFIRMED);
            eventRequestStatusUpdateResult.setConfirmedRequests(requestDtoEvents);
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
        }
        eventRepository.save(event);
        requestRepository.saveAll(requests);
        return eventRequestStatusUpdateResult;
    }

    private List<RequestDtoEvent> setRequestStatus(List<RequestEvent> requests, Status status) {
        requests.forEach(r -> r.setStatus(status));
        return toParticipationRequestDtos(requests);
    }

    public List<RequestDtoEvent> toParticipationRequestDtos(List<RequestEvent> requests) {
        List<RequestDtoEvent> requestDtoEvents = RequestMapper.toRequestDtoEvents(requests);
        for (int i = 0; i < requests.size(); i++) {
            RequestEvent requestEvent = requests.get(i);
            requestDtoEvents.get(i).setEvent(requestEvent.getEvent().getId());
            requestDtoEvents.get(i).setRequester(requestEvent.getRequester().getId());
        }
        return requestDtoEvents;
    }

    public List<EventDto> getEvents(
            List<Integer> users,
            List<State> states,
            List<Integer> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    ) {
        List<Event> events;
        rangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();
        rangeEnd = rangeEnd != null ? rangeEnd : rangeStart.plusYears(1);

        if (rangeEnd.isBefore(rangeStart))
            throw new BadRequestException("end is before start!");

        if (users != null && states != null && categories != null) {
            events = eventRepository
                    .findAllByInitiator_IdInAndStateInAndCategory_IdInAndEventDateBeforeAndEventDateAfter(
                            users, states, categories, rangeEnd, rangeStart, PageRequest.of(from, size))
                    .getContent();
        } else {
            events = eventRepository.findAllByEventDateBeforeAndEventDateAfter(rangeEnd, rangeStart,
                    PageRequest.of(from, size)).getContent();
        }
        return eventMapper.toEventFullDtos(events);
    }

    public EventDto patchEvent(Integer eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest != null) {
            if (updateEventAdminRequest.getEventDate() != null
                    && LocalDateTime.now().plusHours(1).isAfter(updateEventAdminRequest.getEventDate()))
                    throw new ForbiddenException("date error");

            Event event = eventRepository.findById(eventId).orElseThrow(NotFoundException::new);
            if (updateEventAdminRequest.getStateAction() == PUBLISH_EVENT
                    && (event.getState() == PUBLISHED || event.getState() == State.CANCELED)) {
                throw new ConflictException("");
            }
            if (updateEventAdminRequest.getStateAction() == REJECT_EVENT && event.getState() == PUBLISHED) {
                throw new ConflictException("");
            }
            eventMapper.updateEvent(event, updateEventAdminRequest);
            if (updateEventAdminRequest.getCategory() != null) {
                Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                        .orElseThrow(NotFoundException::new);
                event.setCategory(category);
            }
            if (updateEventAdminRequest.getStateAction() == PUBLISH_EVENT) event.setState(PUBLISHED);
            if (updateEventAdminRequest.getStateAction() == REJECT_EVENT) event.setState(State.CANCELED);
            Event savedEvent = eventRepository.save(event);
            return eventMapper.toEventFullDto(savedEvent);
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("e"));
        if (event.getState() == PUBLISHED) throw new ConflictException("event state is PUBLISHED!");
        return eventMapper.toEventFullDto(event);
    }

    public List<EventShortDto> getEvents(
            String text,
            List<Integer> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            Integer from,
            Integer size,
            String ip
    ) {
        List<Event> events;
        rangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();
        rangeEnd = rangeEnd != null ? rangeEnd : rangeStart.plusYears(1);

        if (rangeEnd.isBefore(rangeStart))
            throw new BadRequestException("end is before start!");

        if (text != null && paid != null && sort != null && categories != null) {
            events = eventRepository.findAllByAnnotationContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategory_IdInAndPaidIsAndEventDateBeforeAndEventDateAfter(
                            text, text, categories, paid, rangeEnd, rangeStart, PageRequest.of(from, size)).getContent();
        } else {
            events = eventRepository.findAllByEventDateBeforeAndEventDateAfter(rangeEnd, rangeStart,
                    PageRequest.of(from, size)).getContent();
        }
        HitDto endpointHitDto = HitDto.builder().app("ewm-main-service").uri("/events")
                .timestamp(LocalDateTime.now()).ip(ip).build();
        statsClient.createHit(endpointHitDto);
        return eventMapper.toEventShortDtos(events);
    }

    public EventDto getEvent(Integer eventId, String ip) {
        Event event = eventRepository.findByIdAndStateIn(eventId, List.of(PUBLISHED)).orElseThrow(NotFoundException::new);
        HitDto endpointHitDto = HitDto.builder().app("ewm-main-service").uri("/events/" + eventId)
                .timestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)).ip(ip).build();
        if (event.getViews() == null) event.setViews(1L);
        else event.setViews(event.getViews() + 1);
        statsClient.createHit(endpointHitDto);
        return eventMapper.toEventFullDto(event);
    }

    public  EventDto findFirstByCategoryId(Integer catId) {
        Event event = eventRepository.findFirstByCategoryId(catId);
        return event != null ? eventMapper.toEventFullDto(eventRepository.findFirstByCategoryId(catId)) : null;
    }

    public List<Event> findAllByIdIn(List<Integer> events) {
        return eventRepository.findAllByIdIn(events);
    }

    public Event findById(Integer id){
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException("not found event with id = " + id));
    }
}
