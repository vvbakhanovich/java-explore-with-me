package ru.practicum.yandex.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.compilation.dto.NewCompilationDto;
import ru.practicum.yandex.compilation.dto.UpdateCompilationRequest;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.compilation.repository.CompilationRepository;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.repository.EventRepository;
import ru.practicum.yandex.shared.exception.NotFoundException;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    @Override
    public Compilation addCompilation(NewCompilationDto newCompilationDto) {
        List<Long> compilationEventIds = newCompilationDto.getEvents();
        List<Event> compilationEvents = eventRepository.findAllById(compilationEventIds);
        Compilation compilation = Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.isPinned())
                .events(new LinkedHashSet<>(compilationEvents))
                .build();
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Compilation with id '{}' was saved.", savedCompilation.getId());
        return savedCompilation;
    }

    @Override
    public Compilation updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = getCompilation(compId);
        updateCompilationIfNeeded(updateRequest, compilation);
        log.info("Compilation with id '{}' was updated.", compId);
        return compilation;
    }

    @Override
    public void deleteCompilation(Long compId) {
        getCompilation(compId);
        compilationRepository.deleteById(compId);
        log.info("Compilation with id '{}' was deleted.", compId);
    }

    private void updateCompilationIfNeeded(UpdateCompilationRequest updateRequest, Compilation compilation) {
        if (updateRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateRequest.getEvents());
            compilation.setEvents(new LinkedHashSet<>(events));
        }
        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id '" + compId + "' was not found."));
    }
}
