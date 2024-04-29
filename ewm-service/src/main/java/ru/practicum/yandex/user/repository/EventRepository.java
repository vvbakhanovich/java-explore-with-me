package ru.practicum.yandex.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.yandex.user.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e JOIN FETCH e.category c JOIN FETCH e.initiator i WHERE i.id = ?1")
    List<Event> findEventsByUserId(Long userId, Pageable pageable);

    long countEventsByCategoryId(Long categoryId);
}
