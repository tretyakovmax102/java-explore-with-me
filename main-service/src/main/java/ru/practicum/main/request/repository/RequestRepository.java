package ru.practicum.main.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.request.model.RequestEvent;
import ru.practicum.main.request.model.Status;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<RequestEvent, Integer> {
    List<RequestEvent> findAllByRequester_Id(Integer userId);

    List<RequestEvent> findAllByEvent_Id(Integer eventId);

    List<RequestEvent> findAllByRequester_IdAndEvent_Id(Integer userId, Integer eventId);

    Boolean existsParticipationRequestByRequester_idAndEvent_Id(Integer userId, Integer eventId);

    Boolean existsParticipationRequestByIdInAndStatus(List<Integer> ids, Status status);
}
