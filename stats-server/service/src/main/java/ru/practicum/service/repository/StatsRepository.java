package ru.practicum.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.StatsDto;
import ru.practicum.service.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT new ru.practicum.dto.StatsDto(a.app, a.uri,  count(a)) " +
            "FROM Hit a " +
            "WHERE a.created > :start AND a.created < :end " +
            "GROUP BY a.app, a.uri " +
            "ORDER BY count(DISTINCT a.ip) DESC")
    List<StatsDto> getViewStatisticsWithoutUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.StatsDto(a.app, a.uri,  count(DISTINCT a.ip))" +
            "FROM Hit a " +
            "WHERE a.created BETWEEN :start AND :end " +
            "GROUP BY a.app, a.uri " +
            "ORDER BY count(a.ip) DESC ")
    List<StatsDto> getViewStatisticsWithoutUrisAndIsIpUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.StatsDto(a.app, a.uri,  count(a)) " +
            "FROM Hit a WHERE a.uri IN :uris  " +
            "AND  a.created > :start AND a.created < :end " +
            "GROUP BY a.app, a.uri " +
            "ORDER BY count(a) DESC ")
    List<StatsDto> getViewStatisticsWithUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.dto.StatsDto(a.app, a.uri,  count(DISTINCT a.ip)) " +
            "FROM Hit a WHERE a.uri IN :uris  " +
            "AND  a.created > :start AND a.created < :end " +
            "GROUP BY a.app, a.uri " +
            "ORDER BY count(DISTINCT a.ip) DESC")
    List<StatsDto> getViewStatisticsWithUrisAndIpIsUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, List<String> uris);
}