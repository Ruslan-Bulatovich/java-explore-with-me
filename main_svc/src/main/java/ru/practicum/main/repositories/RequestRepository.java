package ru.practicum.main.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.enums.RequestStatus;
import ru.practicum.main.models.Event;
import ru.practicum.main.models.Request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEvent_Initiator_Id(Long userId);

    Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    Optional<Request> findByRequesterIdAndId(Long userId, Long requestId);

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventInAndStatus(List<Event> events, RequestStatus status);

    Boolean existsByEventIdAndRequesterIdAndStatusAndEvent_EventDateBefore(Long eventId, Long userId, RequestStatus status, LocalDateTime time);

}
