package ru.practicum.main.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.main.request.model.Status;

import java.time.LocalDateTime;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class RequestDtoEvent {
    Integer id;
    LocalDateTime created;
    Integer event;
    Integer requester;
    Status status;
}
