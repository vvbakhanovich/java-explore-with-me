package ru.practicum.yandex.events.service;

import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.user.model.Event;

import java.util.List;

public interface EventService {
    List<Event> findEvents(EventSearchFilter searchFilter, Long from, Integer size);

    Event getFullEventInfoById(Long id);
}
