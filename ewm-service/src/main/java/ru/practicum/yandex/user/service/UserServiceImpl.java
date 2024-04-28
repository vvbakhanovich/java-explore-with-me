package ru.practicum.yandex.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.EventNotModifiableException;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.shared.exception.RequestAlreadyExistsException;
import ru.practicum.yandex.user.dto.UpdateEventUserRequest;
import ru.practicum.yandex.user.mapper.EventMapper;
import ru.practicum.yandex.user.model.Event;
import ru.practicum.yandex.user.model.EventState;
import ru.practicum.yandex.user.model.Location;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.model.ParticipationStatus;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.repository.EventRepository;
import ru.practicum.yandex.user.repository.LocationRepository;
import ru.practicum.yandex.user.repository.ParticipationRequestRepository;
import ru.practicum.yandex.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    private final LocationRepository locationRepository;

    private final ParticipationRequestRepository participationRequestRepository;

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
    public Event addEventByUser(Long userId, NewEvent newEvent) {
        final User initiator = getUser(userId);
        final Category category = getCategory(newEvent);
        final Location eventLocation = saveLocation(newEvent);
        Event fullEvent;
        fullEvent = createEvent(newEvent, category, initiator, eventLocation);
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

    @Override
    public Event getFullEventByInitiator(Long userId, Long eventId) {
        return null;
    }

    @Override
    public Event updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        getUser(userId);
        Event eventToUpdate = getEvent(eventId);
        EventState eventState = eventToUpdate.getState();
        //TODO delete if not needed
//        checkEventState(eventState);
        changeStateIfNeeded(updateEvent, eventToUpdate);
        eventMapper.updateEvent(updateEvent, eventToUpdate);
        log.info("UserService, event with id '{}' was updated by user with id '{}'.", eventId, userId);
        return eventToUpdate;
    }

    @Override
    public ParticipationRequest addParticipationRequestToEvent(Long userId, Long eventId) {
        User user = getUser(userId);
        final Event event = getEvent(eventId);
        checkIfUserCanMakeRequest(userId, eventId, event);
        checkIfParticipationRequestExists(userId, eventId);
        checkIfEventIsPublished(event);
        return createParticipantRequest(eventId, user, event);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id '" + userId + "' not found."));
    }

    private Event createEvent(NewEvent newEvent, Category category, User initiator, Location eventLocation) {
        Event fullEvent;
        if (newEvent.isRequestModeration()) {
            fullEvent = eventMapper.toFullEvent(newEvent, category, initiator, EventState.PENDING, eventLocation);
        } else {
            fullEvent = eventMapper.toFullEvent(newEvent, category, initiator, EventState.PUBLISHED, eventLocation);
            fullEvent.setPublishedOn(LocalDateTime.now());
        }
        return fullEvent;
    }

    private Location saveLocation(NewEvent newEvent) {
        final Location eventLocation = locationRepository.save(newEvent.getLocation());
        log.info("UserService, location '{}' was saved.", eventLocation);
        return eventLocation;
    }

    private Category getCategory(NewEvent newEvent) {
        return categoryRepository.findById(newEvent.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category with id '" + newEvent.getCategoryId() + "' not found."));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id '" + eventId + "' was not found."));
    }

    private void checkEventState(EventState eventState) {
        if (!(eventState.equals(EventState.CANCELED) || eventState.equals(EventState.PENDING))) {
            throw new EventNotModifiableException("Event can not be modified.");
        }
    }

    private void changeStateIfNeeded(UpdateEventUserRequest updateEvent, Event eventToUpdate) {
        switch (updateEvent.getStateAction()) {
            case CANCEL_REVIEW:
                eventToUpdate.setState(EventState.CANCELED);
                break;
            case SEND_TO_REVIEW:
                eventToUpdate.setState(EventState.PENDING);
                break;
        }
    }

    private void checkIfParticipationRequestExists(Long userId, Long eventId) {
        Optional<ParticipationRequest> participationRequest = participationRequestRepository.findByRequesterId(userId);
        if (participationRequest.isPresent()) {
            throw new RequestAlreadyExistsException("Participation request by user with id '" + userId + "' to event " +
                    "with id '" + eventId + "' already exists. ");
        }
    }

    private void checkIfUserCanMakeRequest(Long userId, Long eventId, Event event) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new NotAuthorizedException("Initiator with id '" + userId + "' can not make participation request " +
                    "to his own event with id '" + eventId + "'.");
        }
    }

    private void checkIfEventIsPublished(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotAuthorizedException("User can not make request to not published event.");
        }
    }

    private ParticipationRequest createParticipantRequest(Long eventId, User user, Event event) {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .build();
        if (!event.isRequestModeration()) {
            participationRequest.setStatus(ParticipationStatus.CONFIRMED);
        } else if (event.getParticipantLimit() <= participationRequestRepository.countByEventId(eventId)) {
            participationRequest.setStatus(ParticipationStatus.PENDING);
        } else {
            throw new NotFoundException("Participant limit is exceeded.");
        }
        return participationRequest;
    }
}
