package ru.practicum.main.compilation.model;

import org.mapstruct.*;
import org.springframework.stereotype.Component;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto dto);

    CompilationDto toCompilationDto(Compilation compilation);

    List<CompilationDto> toCompilationDtos(List<Compilation> categories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "events", ignore = true)
    default void updateCompilation(@MappingTarget Compilation compilation, UpdateCompilationRequest dto) {
    }

}
