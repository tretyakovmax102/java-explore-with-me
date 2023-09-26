package ru.practicum.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatsDto;
import ru.practicum.service.model.StatsMapper;
import ru.practicum.dto.HitDto;
import ru.practicum.service.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository statsRepository;

    public void createHit(HitDto hitDto) {
        statsRepository.save(StatsMapper.toDto(hitDto));
    }

    public List<StatsDto> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris == null || uris.isEmpty())
            return unique ?
                    statsRepository.findViewStatisticsWithoutUrisAndIsIpUnique(start, end) :
                    statsRepository.findViewStatisticsWithoutUris(start, end);
        return unique ?
                statsRepository.findViewStatisticsWithUrisAndIpIsUnique(start, end, uris) :
                statsRepository.findViewStatisticsWithUris(start, end, uris);
    }

}