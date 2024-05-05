package ru.practicum.yandex.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.events.dto.EventFullDto;
import ru.practicum.yandex.events.dto.EventShortDto;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateDto;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.dto.ParticipationRequestDto;
import ru.practicum.yandex.user.mapper.ParticipationMapper;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    private final EventMapper eventMapper;

    private final ParticipationMapper participationMapper;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("User with id '{}' adding new event '{}'.", userId, newEventDto.getTitle());
        final NewEvent newEvent = eventMapper.toModel(newEventDto);
        final Event addedEvent = userService.addEventByUser(userId, newEvent);
        return eventMapper.toDto(addedEvent);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> findEventsFromUser(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Finding events from user with id '{}'.", userId);
        final List<Event> events = userService.findEventsFromUser(userId, from, size);
        return eventMapper.toShortDtoList(events);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getFullEventByInitiator(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Requesting full event with id '{}' by user with id '{}.", eventId, userId);
        final Event event = userService.getFullEventByInitiator(userId, eventId);
        return eventMapper.toDto(event);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid EventUpdateRequest updateEvent) {
        log.info("Updating event with id '{}', by user with id '{}'.", eventId, userId);
        final Event updatedEvent = userService.updateEvent(userId, eventId, updateEvent);
        return eventMapper.toDto(updatedEvent);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequestToEvent(@PathVariable Long userId,
                                                                  @RequestParam Long eventId) {
        log.info("User with id '{}' requesting participation in event with id '{}'.", userId, eventId);
        final ParticipationRequest participationRequest = userService.addParticipationRequestToEvent(userId, eventId);
        return participationMapper.toDto(participationRequest);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> findParticipationRequestsByUser(@PathVariable Long userId) {
        log.info("User with id '{}' requesting participation request list.", userId);
        final List<ParticipationRequest> participationRequests = userService.findParticipationRequestsByUser(userId);
        return participationMapper.toDtoList(participationRequests);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelOwnParticipationRequest(@PathVariable Long userId,
                                                                 @PathVariable Long requestId) {
        log.info("User with id '{}' canceling request with id '{}'.", userId, requestId);
        final ParticipationRequest canceledRequest = userService.cancelOwnParticipationRequest(userId, requestId);
        return participationMapper.toDto(canceledRequest);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> findParticipationRequestsForUsersEvent(@PathVariable Long userId,
                                                                                @PathVariable Long eventId) {
        log.info("Getting participation requests in event with id '{}' initiated by user with id '{}'.",
                eventId, userId);
        final List<ParticipationRequest> participationRequests = userService
                .findParticipationRequestsForUsersEvent(userId, eventId);
        return participationMapper.toDtoList(participationRequests);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateDto changeParticipationRequestStatusForUsersEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest statusUpdate) {
        log.info("Changing participation requests status for event with id '{}' by user with id '{}'.", eventId, userId);
        return userService.changeParticipationRequestStatusForUsersEvent(userId, eventId, statusUpdate);
    }
}
