package ru.practicum.main.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.compilation.model.CompilationMapper;
import ru.practicum.main.compilation.repository.CompilationRepository;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationService {

    CompilationRepository compilationRepository;
    CompilationMapper compilationMapper;
    EventService eventService;

    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(PageRequest.of(from, size)).getContent();
        } else {
            compilations = compilationRepository
                    .findCompilationsByPinnedTrue(PageRequest.of(from, size)).getContent();
        }
        return compilationMapper.toCompilationDtos(compilations);
    }

    public CompilationDto getCompilation(Integer compId) {
        return compilationMapper.toCompilationDto(
                compilationRepository.findById(compId).orElseThrow(
                        () -> new NotFoundException("not found competition with id =" + compId)));
    }

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        if (newCompilationDto.getEvents() != null)
            compilation.setEvents(eventService.findAllByIdIn(newCompilationDto.getEvents()));
        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    public void deleteCompilation(Integer compId) {
        getCompilation(compId);
        compilationRepository.deleteById(compId);
    }

    public CompilationDto updateCompilation(Integer compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("not found competition with id =" + compId));
        compilationMapper.updateCompilation(compilation, updateCompilationRequest);
        if (updateCompilationRequest.getEvents() != null)
            compilation.setEvents(eventService.findAllByIdIn(updateCompilationRequest.getEvents()));
        compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(compilation);
    }
}
