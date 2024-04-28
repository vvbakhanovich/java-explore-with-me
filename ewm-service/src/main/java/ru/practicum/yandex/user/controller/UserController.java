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
import ru.practicum.yandex.user.dto.EventFullDto;
import ru.practicum.yandex.user.dto.EventShortDto;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.dto.ParticipationRequestDto;
import ru.practicum.yandex.user.dto.UpdateEventUserRequest;
import ru.practicum.yandex.user.mapper.EventMapper;
import ru.practicum.yandex.user.mapper.ParticipationMapper;
import ru.practicum.yandex.user.model.Event;
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
        log.info("UserController, user with id '{}' adding new event '{}'.", userId, newEventDto.getTitle());
        final NewEvent newEvent = eventMapper.toModel(newEventDto);
        final Event addedEvent = userService.addEventByUser(userId, newEvent);
        return eventMapper.toDto(addedEvent);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> findEventsFromUser(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("UserController, finding events from user with id '{}'.", userId);
        final List<Event> events = userService.findEventsFromUser(userId, from, size);
        return eventMapper.toShortDtoList(events);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getFullEventByInitiator(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("UserController, get full event with id '{}' by user with id '{}.", eventId, userId);
        final Event event = userService.getFullEventByInitiator(userId, eventId);
        return eventMapper.toDto(event);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEvent) {
        log.info("UserController, update event with id '{}', by user with id '{}'.", eventId, userId);
        final Event updatedEvent = userService.updateEvent(userId, eventId, updateEvent);
        return eventMapper.toDto(updatedEvent);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequestToEvent(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("UserController, user with id '{}' requesting participation in event with id'{}'.", userId, eventId);
        final ParticipationRequest participationRequest = userService.addParticipationRequestToEvent(userId, eventId);
        return participationMapper.toDto(participationRequest);
    }
}
