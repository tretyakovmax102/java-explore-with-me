package ru.practicum.main.compilation.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.main.event.dto.EventShortDto;

import java.util.List;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    Integer id;
    Boolean pinned;
    String title;
    List<EventShortDto> events;
}
