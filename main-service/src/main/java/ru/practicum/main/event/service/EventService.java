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
import ru.practicum.main.comment.model.CommentMapper;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.model.*;
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

import static ru.practicum.main.event.model.State.PUBLISHED;

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
    CommentRepository commentRepository;

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
        toSave.setState(State.PENDING);
        toSave.setConfirmedRequests(0);
        if (createEventDto.getPaid() == null){
            toSave.setPaid(false);
        }
        if (createEventDto.getParticipantLimit() == null){
            toSave.setParticipantLimit(0);
        }
        if (createEventDto.getRequestModeration() == null){
            toSave.setRequestModeration(true);
        }
        eventRepository.save(toSave);
        return eventMapper.toEventFullDto(toSave);
    }

    public EventDto getEvent(Integer userId, Integer eventId) {
        EventDto event = eventMapper.toEventFullDto(eventRepository.findByIdAndInitiator_Id(eventId, userId)
                .orElseThrow(NotFoundException::new));
        event.setComments(CommentMapper.commentToDto(commentRepository.findAllByEventId(eventId)));
        return event;
    }

    public EventDto changeEvent(Integer userId, Integer eventId, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getEventDate() != null &&
                LocalDateTime.now().plusHours(1).isAfter(updateEventUserRequest.getEventDate())) {
            throw new ForbiddenException("date error");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(NotFoundException::new);
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("state = published");
        }
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("player isn't initiator");
        }
        eventMapper.updateEvent(event, updateEventUserRequest);
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(NotFoundException::new);
            event.setCategory(category);
        }
        if (updateEventUserRequest.getStateAction() == StateActionUser.SEND_TO_REVIEW) {
            event.setState(State.PENDING);
        }
        if (updateEventUserRequest.getStateAction() == StateActionUser.CANCEL_REVIEW) {
            event.setState(State.CANCELED);
        }
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
        if (event.getConfirmedRequests() != null && event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new BadRequestException("eventRequest cannot be patched");
        }
        if (requestRepository.existsParticipationRequestByIdInAndStatus
                (eventRequestStatusUpdateRequest.getRequestIds(), Status.CONFIRMED)) {
            throw new ConflictException("event status is PENDING!");
        }
        if (event.getConfirmedRequests() != null && event.getConfirmedRequests().equals(event.getParticipantLimit())) {
            throw new ConflictException("eventRequest cannot be patched");
        }
        List<RequestEvent> requests = requestRepository.findAllById(eventRequestStatusUpdateRequest.getRequestIds());
        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = new EventRequestStatusUpdateResult();
        if (eventRequestStatusUpdateRequest.getStatus() == Status.REJECTED) {
            List<RequestDtoEvent> requestDtoEvents = setRequestStatus(requests, Status.REJECTED);
            eventRequestStatusUpdateResult.setRejectedRequests(requestDtoEvents);
            event.setConfirmedRequests(event.getConfirmedRequests() - requests.size());
        } else {
            List<RequestDtoEvent> requestDtoEvents = setRequestStatus(requests, Status.CONFIRMED);
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

        if (rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("end is before start!");
        }
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

    private void checkEventDate(UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getEventDate() != null
                && LocalDateTime.now().plusHours(1).isAfter(updateEventAdminRequest.getEventDate())) {
            throw new ForbiddenException("date error");
        }
    }

    private void checkEventStatus(Integer eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(NotFoundException::new);
        if (updateEventAdminRequest.getStateAction() == StateActionAdmin.PUBLISH_EVENT
                && (event.getState() == State.PUBLISHED || event.getState() == State.CANCELED)) {
            throw new ConflictException("");
        }
        if (updateEventAdminRequest.getStateAction() == StateActionAdmin.REJECT_EVENT && event.getState() == State.PUBLISHED) {
            throw new ConflictException("");
        }
        eventMapper.updateEvent(event, updateEventAdminRequest);
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(NotFoundException::new);
            event.setCategory(category);
        }
    }

    public EventDto patchEvent(Integer eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        checkEventDate(updateEventAdminRequest);
        checkEventStatus(eventId, updateEventAdminRequest);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("e"));
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("event state is PUBLISHED!");
        }
        if (updateEventAdminRequest.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
            event.setState(State.PUBLISHED);
        }
        if (updateEventAdminRequest.getStateAction() == StateActionAdmin.REJECT_EVENT) {
            event.setState(State.CANCELED);
        }
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(savedEvent);
    }

    public List<EventShortDto> getEvents(
            String text,
            List<Integer> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            String sort,
            Integer from,
            Integer size,
            String ip
    ) {
        List<Event> events;
        rangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();
        rangeEnd = rangeEnd != null ? rangeEnd : rangeStart.plusYears(1);

        if (rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("end is before start!");
        }
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
        if (event.getViews() == null){
            event.setViews(1L);
        } else {
            event.setViews(event.getViews() + 1);
        }
        statsClient.createHit(endpointHitDto);
        EventDto eventDto = eventMapper.toEventFullDto(event);
        eventDto.setComments(CommentMapper.commentToDto(commentRepository.findAllByEventId(eventId)));
        return eventDto;
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
