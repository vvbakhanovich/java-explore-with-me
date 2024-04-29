package ru.practicum.yandex.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.model.Event;
import ru.practicum.yandex.user.model.EventState;
import ru.practicum.yandex.user.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public List<Event> findEvents(EventSearchFilter searchFilter, Long from, Integer size) {
        Sort sort = getSort(searchFilter);
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size, sort);
        List<Specification<Event>> specifications = eventSearchFilterToSpecifications(searchFilter);
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null),
                pageRequest).getContent();
        log.info("Requesting events with filter '{}'.", searchFilter);
        if (searchFilter.isOnlyAvailable()) {
            return returnOnlyAvailableIfNeeded(events);
        }
        return events;
    }

    private static List<Event> returnOnlyAvailableIfNeeded(List<Event> events) {
        return events.stream()
                .filter(event -> event.getParticipants() < event.getParticipantLimit())
                .collect(Collectors.toList());
    }

    @Override
    public Event getFullEventInfoById(Long id) {
        Event event = getEvent(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotAuthorizedException("Event is not published.");
        }
        log.info("Requesting full event info with id '{}'.", id);
        return event;
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id '" + id + "' was not found."));
    }

    private Sort getSort(EventSearchFilter searchFilter) {
        Sort sort;
        switch (searchFilter.getSort()) {
            case VIEWS:
                sort = Sort.by(Sort.Direction.ASC, "views");
                break;
            case EVENT_DATE:
                sort = Sort.by(Sort.Direction.ASC, "eventDate");
                break;
            default:
                throw new IllegalArgumentException("Sort '" + searchFilter.getSort() + "is not supported yet.");
        }
        return sort;
    }

    private List<Specification<Event>> eventSearchFilterToSpecifications(EventSearchFilter searchFilter) {
        List<Specification<Event>> resultSpecification = new ArrayList<>();
        resultSpecification.add(statusIs(EventState.PUBLISHED));
        resultSpecification.add(searchFilter.getText() == null ? null : textInAnnotationOrDescription(searchFilter.getText()));
        resultSpecification.add(searchFilter.getCategories() == null ? null : inCategories(searchFilter.getCategories()));
        resultSpecification.add(searchFilter.getPaid() == null ? null : isPaid(searchFilter.getPaid()));
        resultSpecification.add(searchFilter.getRangeEnd() == null && searchFilter.getRangeStart() == null ?
                afterDate(LocalDateTime.now()) : inDateRange(searchFilter.getRangeStart(), searchFilter.getRangeEnd()));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Specification<Event> textInAnnotationOrDescription(String text) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(root.get("annotation"),
                        "%" + text.toLowerCase() + "%"),
                criteriaBuilder.like(root.get("description"),
                        "%" + text.toLowerCase() + "%"));
    }

    private Specification<Event> inCategories(List<Long> categoryIds) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("category").get("id")).value(categoryIds);
    }

    private Specification<Event> isPaid(boolean isPaid) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), isPaid));
    }

    private Specification<Event> inDateRange(LocalDateTime startRange, LocalDateTime endRange) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("eventDate"), startRange, endRange);
    }

    private Specification<Event> afterDate(LocalDateTime dateTime) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("eventDate"), dateTime);
    }

    private Specification<Event> statusIs(EventState eventState) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("state"), eventState);
    }
}
