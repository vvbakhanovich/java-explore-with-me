package ru.practicum.yandex.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.yandex.compilation.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
}
