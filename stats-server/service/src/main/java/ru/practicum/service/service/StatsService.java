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

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statsRepository.getViewStatisticsWithoutUrisAndIsIpUnique(start, end);
            } else {
                return statsRepository.getViewStatisticsWithoutUris(start, end);
            }
        } else {
            if (unique) {
                return statsRepository.getViewStatisticsWithUrisAndIpIsUnique(start, end, uris);
            } else {
                return statsRepository.getViewStatisticsWithUris(start, end, uris);
            }
        }
    }

    public void createHit(HitDto hitDto) {
        statsRepository.save(StatsMapper.toDto(hitDto));
    }
}