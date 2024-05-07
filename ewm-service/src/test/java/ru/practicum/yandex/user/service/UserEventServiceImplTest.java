package ru.practicum.yandex.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.dto.LocationDto;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;
import ru.practicum.yandex.events.model.Location;
import ru.practicum.yandex.events.repository.CommentRepository;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.dto.StateAction;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class UserEventServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User savedUser1;

    private User savedUser2;

    private Category savedCategory;

    private Location location;

    private Long unknownId;

    @BeforeEach
    void init() {
        User user1 = createUser(1);
        savedUser1 = userService.createUser(user1);
        User user2 = createUser(2);
        savedUser2 = userService.createUser(user2);
        Category category = Category.builder()
                .name("category name")
                .build();
        savedCategory = categoryRepository.save(category);
        location = Location.builder()
                .lat(24F)
                .lon(54.2F)
                .build();
        unknownId = 999L;
    }

    @Test
    @DisplayName("Add event by user")
    void addEventByUser_allFieldsExist_shouldReturnEventWithNotNullId() {
        NewEvent newEvent = createNewEvent(1);

        Event savedEvent = userService.addEventByUser(savedUser1.getId(), newEvent);

        assertThat(savedEvent, notNullValue());
        assertThat(savedEvent.getId(), greaterThan(0L));
        assertThat(savedEvent.getEventDate(), is(newEvent.getEventDate()));
        assertThat(savedEvent.getCategory().getId(), is(savedCategory.getId()));
        assertThat(savedEvent.getDescription(), is(newEvent.getDescription()));
        assertThat(savedEvent.getParticipantLimit(), is(newEvent.getParticipantLimit()));
        assertThat(savedEvent.getAnnotation(), is(newEvent.getAnnotation()));
        assertThat(savedEvent.getLocation().getLat(), is(newEvent.getLocation().getLat()));
        assertThat(savedEvent.getLocation().getLon(), is(newEvent.getLocation().getLon()));
        assertThat(savedEvent.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(savedEvent.getInitiator().getId(), is(savedUser1.getId()));
    }

    @Test
    @DisplayName("Add event by unknown user")
    void addEventByUser_whenUserNotExists_shouldThrowNotFoundException() {
        NewEvent newEvent = createNewEvent(1);

        NotFoundException e = assertThrows(NotFoundException.class, () -> userService
                .addEventByUser(unknownId, newEvent));

        assertThat(e.getMessage(), is("User with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Add event with unknown category")
    void addEventByUser_whenUnknownCategoryId_shouldThrowNotFoundException() {
        NewEvent newEvent = createNewEvent(1);
        newEvent.setCategoryId(unknownId);

        NotFoundException e = assertThrows(NotFoundException.class, () -> userService
                .addEventByUser(savedUser1.getId(), newEvent));

        assertThat(e.getMessage(), is("Category with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Find events from user")
    void findEventsFromUser_whenUserExists_shouldReturnEvents() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        NewEvent newEvent2 = createNewEvent(2);
        Event savedEvent2 = userService.addEventByUser(savedUser1.getId(), newEvent2);

        List<Event> eventsFromUser = userService.findEventsFromUser(savedUser1.getId(), 0L, 10);

        assertThat(eventsFromUser, notNullValue());
        assertThat(eventsFromUser.size(), is(2));
        assertThat(eventsFromUser.get(0).getId(), is(savedEvent1.getId()));
        assertThat(eventsFromUser.get(1).getId(), is(savedEvent2.getId()));
    }

    @Test
    @DisplayName("Find event from user from 2nd element")
    void findEventsFromUser_whenUserExistsFromSecondElement_shouldReturnEvent() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        NewEvent newEvent2 = createNewEvent(2);
        Event savedEvent2 = userService.addEventByUser(savedUser1.getId(), newEvent2);

        List<Event> eventsFromUser = userService.findEventsFromUser(savedUser1.getId(), 1L, 10);

        assertThat(eventsFromUser, notNullValue());
        assertThat(eventsFromUser.size(), is(1));
        assertThat(eventsFromUser.get(0).getId(), is(savedEvent2.getId()));
    }

    @Test
    @DisplayName("Find event from user size 1")
    void findEventsFromUser_whenUserExistsSize1_shouldReturnEvent() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        NewEvent newEvent2 = createNewEvent(2);
        Event savedEvent2 = userService.addEventByUser(savedUser1.getId(), newEvent2);

        List<Event> eventsFromUser = userService.findEventsFromUser(savedUser1.getId(), 0L, 1);

        assertThat(eventsFromUser, notNullValue());
        assertThat(eventsFromUser.size(), is(1));
        assertThat(eventsFromUser.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Find event from unknown user")
    void findEventsFromUser_whenUserUnknown_shouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> userService
                .findEventsFromUser(unknownId, 0L, 10));

        assertThat(e.getMessage(), is("User with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Get event by initiator")
    void getFullEventByInitiator_whenUserIsInitiator_shouldReturnEvent() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);

        Event event = userService.getFullEventByInitiator(savedUser1.getId(), savedEvent1.getId());

        assertThat(event, notNullValue());
        assertThat(event.getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Get event by not initiator")
    void getFullEventByInitiator_whenUserIsNotInitiator_shouldThrowNotAuthorizedException() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class, () -> userService
                .getFullEventByInitiator(savedUser2.getId(), savedEvent1.getId()));

        assertThat(e.getMessage(), is("User with id '" + savedUser2.getId() + "' is not an initiator of event with id '" +
                savedEvent1.getId() + "'."));
    }

    @Test
    @DisplayName("Get event by not existing user")
    void getFullEventByInitiator_whenUserNotFound_shouldThrowNotFoundException() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);

        NotFoundException e = assertThrows(NotFoundException.class, () -> userService
                .getFullEventByInitiator(unknownId, savedEvent1.getId()));

        assertThat(e.getMessage(), is("User with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Update event annotation")
    void updateEvent_whenUpdateOnlyAnnotation_shouldUpdateAnnotation() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .annotation("new event annotation update")
                .build();

        Event updatedEvent = userService.updateEvent(savedUser1.getId(), savedEvent1.getId(), updateRequest);

        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getAnnotation(), is(updateRequest.getAnnotation()));
        assertThat(updatedEvent.getId(), is(savedEvent1.getId()));
        assertThat(updatedEvent.getEventDate(), is(savedEvent1.getEventDate()));
        assertThat(updatedEvent.getCategory().getId(), is(savedEvent1.getCategory().getId()));
        assertThat(updatedEvent.getDescription(), is(savedEvent1.getDescription()));
        assertThat(updatedEvent.getState(), is(savedEvent1.getState()));
        assertThat(updatedEvent.getParticipantLimit(), is(savedEvent1.getParticipantLimit()));
        assertThat(updatedEvent.getLocation().getLat(), is(savedEvent1.getLocation().getLat()));
        assertThat(updatedEvent.getLocation().getLon(), is(savedEvent1.getLocation().getLon()));
        assertThat(updatedEvent.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(updatedEvent.getInitiator().getId(), is(savedUser1.getId()));
    }

    @Test
    @DisplayName("Update event state, cancel")
    void updateEvent_whenUpdateStateCancel_shouldCancelEvent() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        Event updatedEvent = userService.updateEvent(savedUser1.getId(), savedEvent1.getId(), updateRequest);

        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getAnnotation(), is(savedEvent1.getAnnotation()));
        assertThat(updatedEvent.getId(), is(savedEvent1.getId()));
        assertThat(updatedEvent.getState(), is(EventState.CANCELED));
        assertThat(updatedEvent.getEventDate(), is(savedEvent1.getEventDate()));
        assertThat(updatedEvent.getCategory().getId(), is(savedEvent1.getCategory().getId()));
        assertThat(updatedEvent.getDescription(), is(savedEvent1.getDescription()));
        assertThat(updatedEvent.getParticipantLimit(), is(savedEvent1.getParticipantLimit()));
        assertThat(updatedEvent.getLocation().getLat(), is(savedEvent1.getLocation().getLat()));
        assertThat(updatedEvent.getLocation().getLon(), is(savedEvent1.getLocation().getLon()));
        assertThat(updatedEvent.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(updatedEvent.getInitiator().getId(), is(savedUser1.getId()));
    }

    @Test
    @DisplayName("Update event state, cancel and than send to review")
    void updateEvent_whenUpdateStateCancelAndThenSendToReview_shouldMakeStatePending() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        EventUpdateRequest cancelRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();
        userService.updateEvent(savedUser1.getId(), savedEvent1.getId(), cancelRequest);

        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.SEND_TO_REVIEW)
                .build();
        Event updatedEvent = userService.updateEvent(savedUser1.getId(), savedEvent1.getId(), updateRequest);


        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getAnnotation(), is(savedEvent1.getAnnotation()));
        assertThat(updatedEvent.getId(), is(savedEvent1.getId()));
        assertThat(updatedEvent.getState(), is(EventState.PENDING));
        assertThat(updatedEvent.getEventDate(), is(savedEvent1.getEventDate()));
        assertThat(updatedEvent.getCategory().getId(), is(savedEvent1.getCategory().getId()));
        assertThat(updatedEvent.getDescription(), is(savedEvent1.getDescription()));
        assertThat(updatedEvent.getParticipantLimit(), is(savedEvent1.getParticipantLimit()));
        assertThat(updatedEvent.getLocation().getLat(), is(savedEvent1.getLocation().getLat()));
        assertThat(updatedEvent.getLocation().getLon(), is(savedEvent1.getLocation().getLon()));
        assertThat(updatedEvent.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(updatedEvent.getInitiator().getId(), is(savedUser1.getId()));
    }

    @Test
    @DisplayName("Update event description")
    void updateEvent_whenUpdateOnlyDescription_shouldUpdateDescription() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .description("new event description update")
                .build();

        Event updatedEvent = userService.updateEvent(savedUser1.getId(), savedEvent1.getId(), updateRequest);

        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getAnnotation(), is(savedEvent1.getAnnotation()));
        assertThat(updatedEvent.getId(), is(savedEvent1.getId()));
        assertThat(updatedEvent.getEventDate(), is(savedEvent1.getEventDate()));
        assertThat(updatedEvent.getCategory().getId(), is(savedEvent1.getCategory().getId()));
        assertThat(updatedEvent.getDescription(), is(updatedEvent.getDescription()));
        assertThat(updatedEvent.getState(), is(savedEvent1.getState()));
        assertThat(updatedEvent.getParticipantLimit(), is(savedEvent1.getParticipantLimit()));
        assertThat(updatedEvent.getLocation().getLat(), is(savedEvent1.getLocation().getLat()));
        assertThat(updatedEvent.getLocation().getLon(), is(savedEvent1.getLocation().getLon()));
        assertThat(updatedEvent.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(updatedEvent.getInitiator().getId(), is(savedUser1.getId()));
    }

    @Test
    @DisplayName("Update event title")
    void updateEvent_whenUpdateOnlyTitle_shouldUpdateTitle() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .title("new event title update")
                .build();

        Event updatedEvent = userService.updateEvent(savedUser1.getId(), savedEvent1.getId(), updateRequest);

        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getAnnotation(), is(savedEvent1.getAnnotation()));
        assertThat(updatedEvent.getId(), is(savedEvent1.getId()));
        assertThat(updatedEvent.getTitle(), is(updateRequest.getTitle()));
        assertThat(updatedEvent.getEventDate(), is(savedEvent1.getEventDate()));
        assertThat(updatedEvent.getCategory().getId(), is(savedEvent1.getCategory().getId()));
        assertThat(updatedEvent.getDescription(), is(savedEvent1.getDescription()));
        assertThat(updatedEvent.getState(), is(savedEvent1.getState()));
        assertThat(updatedEvent.getParticipantLimit(), is(savedEvent1.getParticipantLimit()));
        assertThat(updatedEvent.getLocation().getLat(), is(savedEvent1.getLocation().getLat()));
        assertThat(updatedEvent.getLocation().getLon(), is(savedEvent1.getLocation().getLon()));
        assertThat(updatedEvent.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(updatedEvent.getInitiator().getId(), is(savedUser1.getId()));
    }

    @Test
    @DisplayName("Update event requestModeration")
    void updateEvent_whenUpdateRequestModeration_shouldUpdateRequestModeration() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .requestModeration(true)
                .build();

        Event updatedEvent = userService.updateEvent(savedUser1.getId(), savedEvent1.getId(), updateRequest);

        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getAnnotation(), is(savedEvent1.getAnnotation()));
        assertThat(updatedEvent.getId(), is(savedEvent1.getId()));
        assertThat(updatedEvent.getTitle(), is(savedEvent1.getTitle()));
        assertThat(updatedEvent.getEventDate(), is(savedEvent1.getEventDate()));
        assertThat(updatedEvent.getCategory().getId(), is(savedEvent1.getCategory().getId()));
        assertThat(updatedEvent.getDescription(), is(savedEvent1.getDescription()));
        assertThat(updatedEvent.getState(), is(savedEvent1.getState()));
        assertThat(updatedEvent.isRequestModeration(), is(updateRequest.getRequestModeration()));
        assertThat(updatedEvent.getParticipantLimit(), is(savedEvent1.getParticipantLimit()));
        assertThat(updatedEvent.getLocation().getLat(), is(savedEvent1.getLocation().getLat()));
        assertThat(updatedEvent.getLocation().getLon(), is(savedEvent1.getLocation().getLon()));
        assertThat(updatedEvent.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(updatedEvent.getInitiator().getId(), is(savedUser1.getId()));
    }

    @Test
    @DisplayName("Update event location")
    void updateEvent_whenUpdateLocation_shouldUpdateLocation() {
        NewEvent newEvent1 = createNewEvent(1);
        Event savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .location(new LocationDto(22F, 22F))
                .build();

        Event updatedEvent = userService.updateEvent(savedUser1.getId(), savedEvent1.getId(), updateRequest);

        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getAnnotation(), is(savedEvent1.getAnnotation()));
        assertThat(updatedEvent.getId(), is(savedEvent1.getId()));
        assertThat(updatedEvent.getTitle(), is(savedEvent1.getTitle()));
        assertThat(updatedEvent.getEventDate(), is(savedEvent1.getEventDate()));
        assertThat(updatedEvent.getCategory().getId(), is(savedEvent1.getCategory().getId()));
        assertThat(updatedEvent.getDescription(), is(savedEvent1.getDescription()));
        assertThat(updatedEvent.getState(), is(savedEvent1.getState()));
        assertThat(updatedEvent.isRequestModeration(), is(savedEvent1.isRequestModeration()));
        assertThat(updatedEvent.getParticipantLimit(), is(savedEvent1.getParticipantLimit()));
        assertThat(updatedEvent.getLocation().getLat(), is(updateRequest.getLocation().getLat()));
        assertThat(updatedEvent.getLocation().getLon(), is(updateRequest.getLocation().getLon()));
        assertThat(updatedEvent.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(updatedEvent.getInitiator().getId(), is(savedUser1.getId()));
    }

    private User createUser(int id) {
        return User.builder()
                .name("name" + id)
                .email("user" + id + "@email.ru")
                .build();
    }

    private NewEvent createNewEvent(int id) {
        return NewEvent.builder()
                .annotation("event annotation test " + id)
                .eventDate(LocalDateTime.of(2030, 3, 13, 11, 23, 43))
                .categoryId(savedCategory.getId())
                .participantLimit(213)
                .description("event description " + id)
                .paid(false)
                .requestModeration(false)
                .title("event title" + id)
                .location(location)
                .build();
    }
}