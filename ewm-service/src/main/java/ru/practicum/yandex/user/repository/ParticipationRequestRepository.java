package ru.practicum.yandex.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.yandex.user.model.ParticipationRequest;

import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    Optional<ParticipationRequest> findByRequesterId(Long requesterId);

    long countByEventId(Long eventId);
}
