package ru.practicum.yandex.user.service;

import ru.practicum.yandex.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(User userToAdd);

    List<User> getUsers(List<Long> ids, Long from, Integer size);

    void deleteUser(Long userId);
}
