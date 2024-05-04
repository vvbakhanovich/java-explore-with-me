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
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.Location;
import ru.practicum.yandex.events.service.EventService;
import ru.practicum.yandex.shared.exception.EventNotModifiableException;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.shared.exception.RequestAlreadyExistsException;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateDto;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.yandex.user.dto.StateAction;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.model.ParticipationStatus;
import ru.practicum.yandex.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class UserParticipationRequestServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventService eventService;

    private User savedUser1;

    private User savedUser2;

    private Category savedCategory;

    private Location location;

    private Long unknownId;

    private Event savedEvent1;

    private Event savedEvent2;

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
        savedEvent1 = userService.addEventByUser(savedUser1.getId(), createNewEvent(1));
        savedEvent2 = userService.addEventByUser(savedUser2.getId(), createNewEvent(2));
        unknownId = 999L;
    }

    @Test
    @DisplayName("Add participation request to not published event")
    void addParticipationRequestToEvent_whenEventIsNotPublished_shouldThrowNotAuthorizedException() {
        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> userService.addParticipationRequestToEvent(savedUser1.getId(), savedEvent2.getId()));

        assertThat(e.getMessage(), is("User with id '" + savedUser1.getId() + "'can not make request to not published event " +
                "with id '" + savedEvent2.getId() + "'."));
    }

    @Test
    @DisplayName("Add participation request to your own event")
    void addParticipationRequestToEvent_whenUserTryToParticipateInHisOwnEvent_shouldThrowNotAuthorizedException() {
        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> userService.addParticipationRequestToEvent(savedUser1.getId(), savedEvent1.getId()));

        assertThat(e.getMessage(), is("Initiator with id '" + savedUser1.getId() + "' can not make participation request " +
                "to his own event with id '" + savedEvent1.getId() + "'."));
    }

    @Test
    @DisplayName("Add participation request by unknown user")
    void addParticipationRequestToEvent_whenUnknownUser_shouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.addParticipationRequestToEvent(unknownId, savedEvent1.getId()));

        assertThat(e.getMessage(), is("User with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Add participation request to unknown event")
    void addParticipationRequestToEvent_whenUnknownEvent_shouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.addParticipationRequestToEvent(savedUser1.getId(), unknownId));

        assertThat(e.getMessage(), is("Event with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Add participation request without moderation")
    void addParticipationRequestToEvent_whenRequestModerationIfFalse_shouldReturnConfirmedParticipationRequest() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);

        ParticipationRequest participationRequest = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        assertThat(participationRequest, notNullValue());
        assertThat(participationRequest.getRequester().getId(), is(savedUser2.getId()));
        assertThat(participationRequest.getEvent().getId(), is(publishedEvent1.getId()));
        assertThat(participationRequest.getStatus(), is(ParticipationStatus.CONFIRMED));
    }

    @Test
    @DisplayName("Add participation request with moderation")
    void addParticipationRequestToEvent_whenRequestModerationIfTrue_shouldReturnPendingParticipationRequest() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);

        ParticipationRequest participationRequest = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        assertThat(participationRequest, notNullValue());
        assertThat(participationRequest.getRequester().getId(), is(savedUser2.getId()));
        assertThat(participationRequest.getEvent().getId(), is(publishedEvent1.getId()));
        assertThat(participationRequest.getStatus(), is(ParticipationStatus.PENDING));
    }

    @Test
    @DisplayName("Add participation request with moderation but no participant limit")
    void addParticipationRequestToEvent_whenParticipantLimit0_shouldReturnConfirmedParticipationRequest() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(0)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);

        ParticipationRequest participationRequest = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        assertThat(participationRequest, notNullValue());
        assertThat(participationRequest.getRequester().getId(), is(savedUser2.getId()));
        assertThat(participationRequest.getEvent().getId(), is(publishedEvent1.getId()));
        assertThat(participationRequest.getStatus(), is(ParticipationStatus.CONFIRMED));
    }

    @Test
    @DisplayName("Add to participation requests by same user to same event")
    void addParticipationRequestToEvent_whenAddTwoRequestsToSameEventBySameUser_shouldThrowRequestAlreadyExistsException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(0)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        userService.addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        RequestAlreadyExistsException e = assertThrows(RequestAlreadyExistsException.class,
                () -> userService.addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId()));

        assertThat(e.getMessage(), is("Participation request by user with id '" + savedUser2.getId() + "' to event " +
                "with id '" + publishedEvent1.getId() + "' already exists."));
    }

    @Test
    @DisplayName("Add to participation request when participation limit is exceeded")
    void addParticipationRequestToEvent_whenParticipationLimitExceeded_shouldThrowRequestAlreadyExistsException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(1)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        userService.addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());
        User savedUser3 = userService.createUser(createUser(3));

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> userService.addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId()));

        assertThat(e.getMessage(), is("Participant limit is exceeded for event with id '" + publishedEvent1.getId() + "'."));
    }

    @Test
    @DisplayName("Find participation requests by user")
    void findParticipationRequestsByUser_whenUserHaveRequests_ShouldReturnRequests() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        Event publishedEvent2 = eventService.updateEventByAdmin(savedEvent2.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent2.getId());

        List<ParticipationRequest> requests = userService.findParticipationRequestsByUser(savedUser3.getId());

        assertThat(requests, notNullValue());
        assertThat(requests.size(), is(2));
        assertThat(requests.get(0).getId(), is(participationRequest1.getId()));
        assertThat(requests.get(1).getId(), is(participationRequest2.getId()));
    }

    @Test
    @DisplayName("Find participation requests by user without requests")
    void findParticipationRequestsByUser_whenUserNotHaveRequests_ShouldReturnEmptyList() {
        List<ParticipationRequest> requests = userService.findParticipationRequestsByUser(savedUser1.getId());

        assertThat(requests, notNullValue());
        assertThat(requests, emptyIterable());
    }

    @Test
    @DisplayName("Cancel participation request")
    void cancelOwnParticipationRequest_whenUserIsRequester_shouldCancelRequest() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(1)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        ParticipationRequest participationRequest = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        ParticipationRequest cancelledRequest = userService
                .cancelOwnParticipationRequest(savedUser2.getId(), participationRequest.getId());

        assertThat(cancelledRequest, notNullValue());
        assertThat(cancelledRequest.getStatus(), is(ParticipationStatus.CANCELED));
        assertThat(cancelledRequest.getRequester().getId(), is(participationRequest.getRequester().getId()));
        assertThat(cancelledRequest.getEvent().getId(), is(participationRequest.getEvent().getId()));
    }

    @Test
    @DisplayName("Cancel participation request by unknown user")
    void cancelOwnParticipationRequest_whenUserNotFound_shouldThrowNotFoundException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(1)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        ParticipationRequest participationRequest = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.cancelOwnParticipationRequest(unknownId, participationRequest.getId()));

        assertThat(e.getMessage(), is("User with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Cancel unknown participation request")
    void cancelOwnParticipationRequest_whenRequestNotFound_shouldThrowNotFoundException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(1)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        ParticipationRequest participationRequest = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.cancelOwnParticipationRequest(savedUser2.getId(), unknownId));

        assertThat(e.getMessage(), is("Participation request with id '" + unknownId + "' was not found."));
    }

    @Test
    @DisplayName("Cancel participation request by other user")
    void cancelOwnParticipationRequest_whenUserIsNotRequester_shouldThrowNotFoundException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(1)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        ParticipationRequest participationRequest = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> userService.cancelOwnParticipationRequest(savedUser1.getId(), participationRequest.getId()));

        assertThat(e.getMessage(), is("User with id '" + savedUser1.getId() + "' is not authorized to cancel participation request with" +
                "id '" + participationRequest.getId() + "'."));
    }

    @Test
    @DisplayName("Find participation requests for an event")
    void findParticipationRequestsForUsersEvent_shouldReturnListOfRequests() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        List<ParticipationRequest> requests = userService.findParticipationRequestsForUsersEvent(savedUser1.getId(),
                publishedEvent1.getId());

        assertThat(requests, notNullValue());
        assertThat(requests.size(), is(2));
        assertThat(requests.get(0).getId(), is(participationRequest1.getId()));
        assertThat(requests.get(1).getId(), is(participationRequest2.getId()));
    }

    @Test
    @DisplayName("Find participation requests for an event without requests")
    void findParticipationRequestsForUsersEvent_whenNoRequests_shouldReturnEmptyList() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);

        List<ParticipationRequest> requests = userService.findParticipationRequestsForUsersEvent(savedUser1.getId(),
                publishedEvent1.getId());

        assertThat(requests, notNullValue());
        assertThat(requests, emptyIterable());
    }

    @Test
    @DisplayName("Find participation requests for an event by other user")
    void findParticipationRequestsForUsersEvent_whenRequesterIsNotInitiator_shouldThrowNotAuthorizedException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> userService.findParticipationRequestsForUsersEvent(savedUser2.getId(), publishedEvent1.getId()));

        assertThat(e.getMessage(), is("User with id '" + savedUser2.getId() + "' is not an initiator of event with id '" +
                publishedEvent1.getId() + "'."));
    }

    @Test
    @DisplayName("Find participation requests by unknown user")
    void findParticipationRequestsForUsersEvent_whenUnknownUser_shouldThrowNotFoundException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.findParticipationRequestsForUsersEvent(unknownId, publishedEvent1.getId()));

        assertThat(e.getMessage(), is("User with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Find participation requests to unknown event")
    void findParticipationRequestsForUsersEvent_whenUnknownEvent_shouldThrowNotFoundException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.findParticipationRequestsForUsersEvent(savedUser1.getId(), unknownId));

        assertThat(e.getMessage(), is("Event with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Reject participation requests")
    void changeParticipationRequestStatusForUsersEvent_whenStatusRejected_shouldSetRejectedToRequests() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        EventRequestStatusUpdateRequest statusUpdateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(participationRequest1.getId(), participationRequest2.getId()))
                .status(ParticipationStatus.REJECTED)
                .build();

        EventRequestStatusUpdateDto eventRequestStatusUpdateDto = userService
                .changeParticipationRequestStatusForUsersEvent(savedUser1.getId(), publishedEvent1.getId(), statusUpdateRequest);

        assertThat(eventRequestStatusUpdateDto, notNullValue());
        assertThat(eventRequestStatusUpdateDto.getConfirmedRequests(), emptyIterable());
        assertThat(eventRequestStatusUpdateDto.getRejectedRequests().size(), is(2));
    }

    @Test
    @DisplayName("Reject one of two participation requests")
    void changeParticipationRequestStatusForUsersEvent_whenStatusRejectedForOneRequest_shouldSetRejectedRequest() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        EventRequestStatusUpdateRequest statusUpdateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(participationRequest1.getId()))
                .status(ParticipationStatus.REJECTED)
                .build();

        EventRequestStatusUpdateDto eventRequestStatusUpdateDto = userService
                .changeParticipationRequestStatusForUsersEvent(savedUser1.getId(), publishedEvent1.getId(), statusUpdateRequest);

        assertThat(eventRequestStatusUpdateDto, notNullValue());
        assertThat(eventRequestStatusUpdateDto.getConfirmedRequests(), emptyIterable());
        assertThat(eventRequestStatusUpdateDto.getRejectedRequests().size(), is(1));
    }

    @Test
    @DisplayName("Confirm all requests")
    void changeParticipationRequestStatusForUsersEvent_whenStatusConfirmed_shouldReturnAllConfirmedRequests() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        EventRequestStatusUpdateRequest statusUpdateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(participationRequest1.getId(), participationRequest2.getId()))
                .status(ParticipationStatus.CONFIRMED)
                .build();

        EventRequestStatusUpdateDto eventRequestStatusUpdateDto = userService
                .changeParticipationRequestStatusForUsersEvent(savedUser1.getId(), publishedEvent1.getId(), statusUpdateRequest);

        assertThat(eventRequestStatusUpdateDto, notNullValue());
        assertThat(eventRequestStatusUpdateDto.getRejectedRequests(), emptyIterable());
        assertThat(eventRequestStatusUpdateDto.getConfirmedRequests().size(), is(2));
    }

    @Test
    @DisplayName("Confirm all requests when participation limit is exceeded")
    void changeParticipationRequestStatusForUsersEvent_whenParticipationLimitExceeded_shouldReturnOneConfirmedRequest() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(1)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        EventRequestStatusUpdateRequest statusUpdateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(participationRequest1.getId(), participationRequest2.getId()))
                .status(ParticipationStatus.CONFIRMED)
                .build();

        EventRequestStatusUpdateDto eventRequestStatusUpdateDto = userService
                .changeParticipationRequestStatusForUsersEvent(savedUser1.getId(), publishedEvent1.getId(), statusUpdateRequest);

        assertThat(eventRequestStatusUpdateDto, notNullValue());
        assertThat(eventRequestStatusUpdateDto.getRejectedRequests().size(), is(1));
        assertThat(eventRequestStatusUpdateDto.getRejectedRequests().get(0).getId(), is(participationRequest2.getId()));
        assertThat(eventRequestStatusUpdateDto.getConfirmedRequests().size(), is(1));
        assertThat(eventRequestStatusUpdateDto.getConfirmedRequests().get(0).getId(), is(participationRequest1.getId()));
    }

    @Test
    @DisplayName("Confirm all requests when participation limit is zero")
    void changeParticipationRequestStatusForUsersEvent_whenParticipationLimitIs0_shouldThrowEventNotModifiableException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .participantLimit(0)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        EventRequestStatusUpdateRequest statusUpdateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(participationRequest1.getId(), participationRequest2.getId()))
                .status(ParticipationStatus.CONFIRMED)
                .build();

        EventNotModifiableException e = assertThrows(EventNotModifiableException.class,
                () -> userService
                        .changeParticipationRequestStatusForUsersEvent(savedUser1.getId(), publishedEvent1.getId(), statusUpdateRequest));

        assertThat(e.getMessage(), is("Event with id '" + publishedEvent1.getId() + "' has no participant limit or " +
                "pre moderation if off. No need to confirm requests. Participant limit: '" + publishedEvent1.getParticipantLimit()
                + "', Moderation: '" + publishedEvent1.isRequestModeration() + "'"));
    }

    @Test
    @DisplayName("Confirm all requests are not pending")
    void changeParticipationRequestStatusForUsersEvent_whenRequestStatusNotPending_shouldThrowEventNotModifiableException() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .requestModeration(true)
                .build();
        Event publishedEvent1 = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        User savedUser3 = userService.createUser(createUser(3));
        ParticipationRequest participationRequest1 = userService
                .addParticipationRequestToEvent(savedUser3.getId(), publishedEvent1.getId());
        ParticipationRequest participationRequest2 = userService
                .addParticipationRequestToEvent(savedUser2.getId(), publishedEvent1.getId());

        EventRequestStatusUpdateRequest confirmUpdateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(participationRequest1.getId(), participationRequest2.getId()))
                .status(ParticipationStatus.CONFIRMED)
                .build();
        userService
                .changeParticipationRequestStatusForUsersEvent(savedUser1.getId(), publishedEvent1.getId(), confirmUpdateRequest);

        EventRequestStatusUpdateRequest rejectUpdateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(participationRequest1.getId(), participationRequest2.getId()))
                .status(ParticipationStatus.REJECTED)
                .build();
        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> userService
                        .changeParticipationRequestStatusForUsersEvent(savedUser1.getId(), publishedEvent1.getId(), rejectUpdateRequest));

        assertThat(e.getMessage(), is("For status change request must have status PENDING. Current status: '"
                + participationRequest1.getStatus() + "'"));
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