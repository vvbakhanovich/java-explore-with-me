package ru.practicum.yandex.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User createUser(User userToAdd) {
        User savedUser = userRepository.save(userToAdd);
        log.info("User with id '{}' created.", savedUser.getId());
        return savedUser;
    }

    @Override
    public List<User> getUsers(List<Long> ids, Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        log.info("UserService get users with ids = '{}', from = '{}', size = '{}'.", ids, from, size);
        if (ids == null) {
            return userRepository.findAll(pageRequest).getContent();
        } else {
            return userRepository.findAllByIdIn(ids, pageRequest).getContent();
        }
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id '" + userId + "' not found."));
        log.info("UserService deleted user with id '" + userId + "'.");
        userRepository.deleteById(userId);
    }
}
