package ru.practicum.main.compilation.model;

import org.mapstruct.*;
import org.springframework.stereotype.Component;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public abstract class CompilationMapper {

    @Mapping(target = "events", ignore = true)
    public abstract Compilation toCompilation(NewCompilationDto dto);

    public abstract CompilationDto toCompilationDto(Compilation compilation);

    public abstract List<CompilationDto> toCompilationDtos(List<Compilation> categories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "events", ignore = true)
    public abstract void updateCompilation(@MappingTarget Compilation compilation, UpdateCompilationRequest dto);
}
