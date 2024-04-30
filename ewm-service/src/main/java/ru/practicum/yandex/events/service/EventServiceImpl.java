package ru.practicum.yandex.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.yandex.events.dto.EventAdminSearchFilter;
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.events.dto.EventSort;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.dto.StateAction;
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

    private final EventMapper eventMapper;

    @Override
    public List<Event> findEvents(EventSearchFilter searchFilter, Long from, Integer size) {
        Sort sort = getSort(searchFilter.getSort());
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size, sort);
        List<Specification<Event>> specifications = eventSearchFilterToSpecifications(searchFilter);
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null),
                pageRequest).getContent();
        log.info("Requesting events with filter '{}'.", searchFilter);
        if (searchFilter.isOnlyAvailable()) {
            return getOnlyAvailableRequests(events);
        }
        return events;
    }

    @Override
    public Event getFullEventInfoById(Long id) {
        Event event = getEvent(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event is not published.");
        }
        log.info("Requesting full event info with id '{}'.", id);
        return event;
    }

    @Override
    public List<Event> getFullEventsInfoByAdmin(EventAdminSearchFilter searchFilter, Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Specification<Event>> specifications = eventAdminSearchFilterToSpecifications(searchFilter);
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null),
                pageRequest).getContent();
        log.info("Requesting full events info by admin  with filter '{}'.", searchFilter);
        if (searchFilter.isOnlyAvailable()) {
            return getOnlyAvailableRequests(events);
        }
        return events;
    }

    @Override
    @Transactional
    public Event updateEventByAdmin(Long eventId, EventUpdateRequest updateRequest) {
        Event event = getEvent(eventId);
        eventMapper.updateEvent(updateRequest, event);
        updateEventState(updateRequest.getStateAction(), event);
        Event savedEvent = eventRepository.save(event);
        log.info("Event with id '{}' was updated by admin.", eventId);
        return savedEvent;
    }

    private void updateEventState(StateAction stateAction, Event event) {
        if (stateAction == null) {
            return;
        }
        switch (stateAction) {
            case PUBLISH_EVENT:
                checkIfEventIsCanceled(event);
                checkIfEventIsAlreadyPublished(event);
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                checkIfEventIsAlreadyPublished(event);
                event.setState(EventState.CANCELED);
                break;
        }
    }

    private static List<Event> getOnlyAvailableRequests(List<Event> events) {
        return events.stream()
                .filter(event -> event.getNumberOfParticipants() < event.getParticipantLimit())
                .collect(Collectors.toList());
    }

    private void checkIfEventIsCanceled(Event event) {
        if (event.getState().equals(EventState.CANCELED)) {
            throw new NotAuthorizedException("Can not publish canceled event.");
        }
    }

    private void checkIfEventIsAlreadyPublished(Event event) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new NotAuthorizedException("Event is already published.");
        }
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id '" + id + "' was not found."));
    }

    private Sort getSort(EventSort eventSort) {
        Sort sort = Sort.unsorted();
        if (eventSort == null) {
            return sort;
        }

        switch (eventSort) {
            case VIEWS:
                sort = Sort.by(Sort.Direction.ASC, "views");
                break;
            case EVENT_DATE:
                sort = Sort.by(Sort.Direction.ASC, "eventDate");
                break;
            default:
                throw new IllegalArgumentException("Sort '" + eventSort + "is not supported yet.");
        }
        return sort;
    }

    private List<Specification<Event>> eventSearchFilterToSpecifications(EventSearchFilter searchFilter) {
        List<Specification<Event>> resultSpecification = new ArrayList<>();
        resultSpecification.add(statusIs(EventState.PUBLISHED));
        resultSpecification.add(searchFilter.getText() == null ? null : textInAnnotationOrDescriptionIgnoreCase(searchFilter.getText()));
        resultSpecification.add(searchFilter.getCategories() == null ? null : inCategories(searchFilter.getCategories()));
        resultSpecification.add(searchFilter.getPaid() == null ? null : isPaid(searchFilter.getPaid()));
        resultSpecification.add(searchFilter.getRangeEnd() == null && searchFilter.getRangeStart() == null ?
                afterDate(LocalDateTime.now()) : inDateRange(searchFilter.getRangeStart(), searchFilter.getRangeEnd()));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Specification<Event>> eventAdminSearchFilterToSpecifications(EventAdminSearchFilter searchFilter) {
        List<Specification<Event>> resultSpecification = new ArrayList<>();
        resultSpecification.add(searchFilter.getStates() == null ? null : statusIn(searchFilter.getStates()));
        resultSpecification.add(searchFilter.getUsers() == null ? null : userIdIn(searchFilter.getUsers()));
        resultSpecification.add(searchFilter.getCategories() == null ? null : inCategories(searchFilter.getCategories()));
        resultSpecification.add(searchFilter.getRangeEnd() == null && searchFilter.getRangeStart() == null ?
                afterDate(LocalDateTime.now()) : inDateRange(searchFilter.getRangeStart(), searchFilter.getRangeEnd()));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Specification<Event> textInAnnotationOrDescriptionIgnoreCase(String text) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                                "%" + text.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                                "%" + text.toLowerCase() + "%")
                );
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

    private Specification<Event> statusIn(List<EventState> states) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("state")).value(states);
    }

    private Specification<Event> userIdIn(List<Long> userIds) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("initiator").get("id")).value(userIds);
    }
}
