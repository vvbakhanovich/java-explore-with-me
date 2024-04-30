package ru.practicum.yandex.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.events.dto.EventAdminSearchFilter;
import ru.practicum.yandex.events.service.EventService;
import ru.practicum.yandex.events.dto.EventFullDto;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventAdminController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    @GetMapping
    public List<EventFullDto> getFullEventsInfoByAdmin(EventAdminSearchFilter searchFilter,
                                                       @RequestParam(defaultValue = "0") Long from,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        log.info("Admin requesting full events info, search filter: '{}'.", searchFilter);
        List<Event> events = eventService.getFullEventsInfo(searchFilter, from, size);
        return eventMapper.toDtoList(events);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @RequestBody @Valid EventUpdateRequest updateRequest) {
        log.info("Admin updating event with id '{}'.", eventId);
        Event event= eventService.updateEventByAdmin(eventId, updateRequest);
        return eventMapper.toDto(event);
    }
}