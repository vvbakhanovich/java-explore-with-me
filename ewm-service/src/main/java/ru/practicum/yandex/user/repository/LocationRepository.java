package ru.practicum.yandex.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.yandex.user.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
