package ru.practicum.main.event.model;

import org.mapstruct.*;
import org.springframework.stereotype.Component;
import ru.practicum.main.event.dto.*;

import java.util.List;
@Mapper(componentModel = "spring")
@Component
public abstract class EventMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "eventDate", source = "dto.eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    public abstract Event toEvent(CreateEventDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "eventDate", source = "dto.eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    public abstract void updateEvent(@MappingTarget Event entity, UpdateEventAdminRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "eventDate", source = "dto.eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    public abstract void updateEvent(@MappingTarget Event entity, UpdateEventUserRequest dto);

    public abstract EventDto toEventFullDto(Event event);

    public abstract List<EventShortDto> toEventDtos(List<Event> events);

    public abstract List<EventDto> toEventFullDtos(List<Event> events);

    public abstract List<EventShortDto> toEventShortDtos(List<Event> events);
}
