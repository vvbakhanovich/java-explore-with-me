package ru.practicum.yandex.user.service;

import ru.practicum.yandex.user.dto.UpdateEventUserRequest;
import ru.practicum.yandex.user.model.Event;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(User userToAdd);

    List<User> getUsers(List<Long> ids, Long from, Integer size);

    void deleteUser(Long userId);

    Event addEvent(Long userId, NewEvent newEvent);

    List<Event> findEventsFromUser(Long userId, Long from, Integer size);

    Event getFullEventByInitiator(Long userId, Long eventId);

    Event updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEvent);
}
