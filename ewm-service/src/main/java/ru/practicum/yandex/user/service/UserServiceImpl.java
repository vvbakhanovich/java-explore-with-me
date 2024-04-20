package ru.practicum.yandex.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.mapper.EventMapper;
import ru.practicum.yandex.user.model.Event;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.repository.EventRepository;
import ru.practicum.yandex.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    private final EventMapper eventMapper;

    @Override
    public User createUser(User userToAdd) {
        final User savedUser = userRepository.save(userToAdd);
        log.info("User with id '{}' created.", savedUser.getId());
        return savedUser;
    }

    @Override
    public List<User> getUsers(List<Long> ids, Long from, Integer size) {
        final OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        log.info("UserService get users with ids = '{}', from = '{}', size = '{}'.", ids, from, size);
        if (ids == null) {
            return userRepository.findAll(pageRequest).getContent();
        } else {
            return userRepository.findAllByIdIn(ids, pageRequest).getContent();
        }
    }

    @Override
    public void deleteUser(Long userId) {
        getUser(userId);
        log.info("UserService deleted user with id '" + userId + "'.");
        userRepository.deleteById(userId);
    }

    @Override
    public Event addEvent(Long userId, NewEvent newEvent) {
        final User initiator = getUser(userId);
        final Category category = categoryRepository.findById(newEvent.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category with id '" + newEvent.getCategoryId() + "' not found."));
        final Event fullEvent = eventMapper.toFullEvent(newEvent, category, initiator);
        final Event savedEvent = eventRepository.save(fullEvent);
        log.info("UserService, event with id '{}' was saved.", savedEvent.getId());
        return savedEvent;
    }

    @Override
    public List<Event> findEventsFromUser(Long userId, Long from, Integer size) {
        getUser(userId);
        final OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        final List<Event> userEvents = eventRepository.findEventsByUserId(userId, pageRequest);
        log.info("UserService, requesting event from user with id '{}'. Events found: '{}'.", userId, userEvents.size());
        return userEvents;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id '" + userId + "' not found."));
    }
}
