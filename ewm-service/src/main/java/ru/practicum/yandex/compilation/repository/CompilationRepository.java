package ru.practicum.yandex.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.yandex.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long>, JpaSpecificationExecutor<Compilation> {

    @Query("SELECT DISTINCT c FROM Compilation c LEFT JOIN FETCH c.events e LEFT JOIN FETCH e.category LEFT JOIN FETCH e.initiator")
    List<Compilation> findCompilationsWithEvents(Specification<Compilation> specification, Pageable pageable);

    @Query("SELECT c FROM Compilation c LEFT JOIN FETCH c.events e LEFT JOIN FETCH e.category LEFT JOIN FETCH e.initiator WHERE c.id = ?1")
    Optional<Compilation> findCompilationWithEventById(Long compId);
}
