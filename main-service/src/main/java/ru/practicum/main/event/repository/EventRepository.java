package ru.practicum.main.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    Page<Event> findAllByInitiator_Id(Integer userId, Pageable pageable);

    List<Event> findAllByIdIn(List<Integer> eventIds);


    Page<Event> findAllByInitiator_IdInAndStateInAndCategory_IdInAndEventDateBeforeAndEventDateAfter(
            List<Integer> userId, List<State> states, List<Integer> categoryId, LocalDateTime start,
            LocalDateTime end, Pageable pageable);

    Page<Event> findAllByEventDateBeforeAndEventDateAfter(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Event> findAllByAnnotationContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategory_IdInAndPaidIsAndEventDateBeforeAndEventDateAfter(
            String text,
            String text2,
            List<Integer> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable pageable);

    Optional<Event> findByIdAndInitiator_Id(Integer eventId, Integer userId);

    Optional<Event> findByIdAndStateIn(Integer eventId, List<State> states);

    Event findFirstByCategoryId(Integer eventId);
}
