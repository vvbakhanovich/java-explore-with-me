package ru.practicum.yandex.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.events.service.EventService;
import ru.practicum.yandex.user.dto.EventFullDto;
import ru.practicum.yandex.user.dto.EventShortDto;
import ru.practicum.yandex.user.mapper.EventMapper;
import ru.practicum.yandex.user.model.Event;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    @GetMapping
    public List<EventShortDto> findEvents(EventSearchFilter searchFilter,
                                          @RequestParam(defaultValue = "0") Long from,
                                          @RequestParam(defaultValue = "10") Integer size) {
        log.info("Requesting events, search filter: '{}'.", searchFilter);
        List<Event> events = eventService.findEvents(searchFilter, from, size);
        return eventMapper.toShortDtos(events);
    }

    @GetMapping("/{id}")
    public EventFullDto getFullEventInfoById(@PathVariable Long id) {
        log.info("Requesting full event info with id '{}'.", id);
        Event event = eventService.getFullEventInfoById(id);
        return eventMapper.toDto(event);
    }
}
