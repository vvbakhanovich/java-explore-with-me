package ru.practicum.yandex.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.model.ParticipationStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByEventIdIn(List<Long> eventIds);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);
}
