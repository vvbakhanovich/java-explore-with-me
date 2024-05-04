package ru.practicum.yandex.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;
import ru.practicum.yandex.events.model.Location;
import ru.practicum.yandex.events.repository.EventRepository;
import ru.practicum.yandex.events.repository.LocationRepository;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.model.ParticipationStatus;
import ru.practicum.yandex.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ParticipationRequestRepositoryTest {

    public static final long UNKNOWN_ID = 999L;
    @Autowired
    private ParticipationRequestRepository participationRequestRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    private ParticipationRequest savedParticipationRequest1;

    private ParticipationRequest savedParticipationRequest2;

    private ParticipationRequest savedParticipationRequest3;

    private ParticipationRequest savedParticipationRequest4;

    private User savedUser1;

    private User savedUser2;

    private User savedUser3;

    private Category savedCategory;

    private Location savedLocation;

    private Event savedEvent1;

    private Event savedEvent2;

    @BeforeEach
    void init() {
        User user1 = createUser(1);
        savedUser1 = userRepository.save(user1);
        User user2 = createUser(2);
        savedUser2 = userRepository.save(user2);
        User user3 = createUser(3);
        savedUser3 = userRepository.save(user3);
        Category category = Category.builder()
                .name("category name")
                .build();
        savedCategory = categoryRepository.save(category);
        Location location = Location.builder()
                .lat(24F)
                .lon(54.2F)
                .build();
        savedLocation = locationRepository.save(location);
        Event event1 = createEvent(1, savedUser1);
        savedEvent1 = eventRepository.save(event1);
        Event event2 = createEvent(2, savedUser2);
        savedEvent2 = eventRepository.save(event2);
        ParticipationRequest participationRequest1 = createRequest(savedUser1, event2);
        savedParticipationRequest1 = participationRequestRepository.save(participationRequest1);
        ParticipationRequest participationRequest2 = createRequest(savedUser2, event1);
        savedParticipationRequest2 = participationRequestRepository.save(participationRequest2);
        ParticipationRequest participationRequest3 = createRequest(savedUser3, event1);
        savedParticipationRequest3 = participationRequestRepository.save(participationRequest3);
        ParticipationRequest participationRequest4 = createRequest(savedUser3, event2);
        savedParticipationRequest4 = participationRequestRepository.save(participationRequest4);
    }

    @Test
    @DisplayName("Find request by requester id and event id")
    void findByRequesterIdAndEventId_whenRequesterAndEventExists_shouldReturnRequest() {
        Optional<ParticipationRequest> participationRequest = participationRequestRepository
                .findByRequesterIdAndEventId(savedUser1.getId(), savedEvent2.getId());

        assertTrue(participationRequest.isPresent());
        assertThat(participationRequest.get(), notNullValue());
        assertThat(participationRequest.get().getId(), is(savedParticipationRequest1.getId()));
    }

    @Test
    @DisplayName("Find request by requester id and event id when requester not exists")
    void findByRequesterIdAndEventId_whenRequesterNotExists_shouldReturnRequest() {
        Optional<ParticipationRequest> participationRequest = participationRequestRepository
                .findByRequesterIdAndEventId(UNKNOWN_ID, savedEvent2.getId());

        assertFalse(participationRequest.isPresent());
    }

    @Test
    @DisplayName("Find all requests by requester id")
    void findAllByRequesterId_whenUserHas2Requests_shouldReturnListOf2Requests() {
        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllByRequesterId(savedUser3.getId());

        assertThat(participationRequests, notNullValue());
        assertThat(participationRequests.size(), is(2));
        assertThat(participationRequests.get(0).getId(), is(savedParticipationRequest3.getId()));
        assertThat(participationRequests.get(1).getId(), is(savedParticipationRequest4.getId()));
    }

    @Test
    @DisplayName("Find all requests by unknown requester id")
    void findAllByRequesterId_whenUnknownRequesterId_shouldReturnEmptyList() {
        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllByRequesterId(UNKNOWN_ID);

        assertThat(participationRequests, notNullValue());
        assertThat(participationRequests, emptyIterable());
    }

    @Test
    @DisplayName("Find requests by event id")
    void findAllByEventId_whenEventHas2Requests_shouldReturnListOf2Requests() {
        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllByEventId(savedEvent1.getId());

        assertThat(participationRequests, notNullValue());
        assertThat(participationRequests.size(), is(2));
        assertThat(participationRequests.get(0).getId(), is(savedParticipationRequest2.getId()));
        assertThat(participationRequests.get(1).getId(), is(savedParticipationRequest3.getId()));
    }

    @Test
    @DisplayName("Find requests by unknown event id")
    void findAllByEventId_whenSearchByUnknownId_shouldReturnEmptyList() {
        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllByEventId(UNKNOWN_ID);

        assertThat(participationRequests, notNullValue());
        assertThat(participationRequests, emptyIterable());
    }

    @Test
    @DisplayName("Find requests by id in list")
    void findAllByIdIn_whenSearchingInExistingIds_ShouldReturnAllRequests() {
        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByIdIn(
                List.of(savedParticipationRequest1.getId(), savedParticipationRequest2.getId(),
                        savedParticipationRequest3.getId(), savedParticipationRequest4.getId()));

        assertThat(participationRequests, notNullValue());
        assertThat(participationRequests.size(), is(4));
        assertThat(participationRequests.get(0).getId(), is(savedParticipationRequest1.getId()));
        assertThat(participationRequests.get(1).getId(), is(savedParticipationRequest2.getId()));
        assertThat(participationRequests.get(2).getId(), is(savedParticipationRequest3.getId()));
        assertThat(participationRequests.get(3).getId(), is(savedParticipationRequest4.getId()));
    }

    @Test
    @DisplayName("Find requests by id in list of 2 requests")
    void findAllByIdIn_whenSearchingInListOf2Ids_ShouldReturn2Requests() {
        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByIdIn(
                List.of(savedParticipationRequest1.getId(), savedParticipationRequest2.getId()));

        assertThat(participationRequests, notNullValue());
        assertThat(participationRequests.size(), is(2));
        assertThat(participationRequests.get(0).getId(), is(savedParticipationRequest1.getId()));
        assertThat(participationRequests.get(1).getId(), is(savedParticipationRequest2.getId()));
    }

    @Test
    @DisplayName("Find requests by id in list of 2 requests with unknown id")
    void findAllByIdIn_whenSearchingInListOf2IdsWithUnknownId_ShouldReturn2Requests() {
        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByIdIn(
                List.of(savedParticipationRequest1.getId(), savedParticipationRequest2.getId(), UNKNOWN_ID));

        assertThat(participationRequests, notNullValue());
        assertThat(participationRequests.size(), is(2));
        assertThat(participationRequests.get(0).getId(), is(savedParticipationRequest1.getId()));
        assertThat(participationRequests.get(1).getId(), is(savedParticipationRequest2.getId()));
    }

    @Test
    @DisplayName("Find requests by unknown ids")
    void findAllByIdIn_whenSearchingByUnknownIds_ShouldReturnEmptyList() {
        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByIdIn(
                List.of(UNKNOWN_ID));

        assertThat(participationRequests, notNullValue());
        assertThat(participationRequests, emptyIterable());
    }


    private Event createEvent(int id, User initiator) {
        return Event.builder()
                .annotation("event annotation test " + id)
                .eventDate(LocalDateTime.of(2030, 3, 13, 11, 23, 43))
                .initiator(initiator)
                .category(savedCategory)
                .participantLimit(213)
                .description("event description " + id)
                .state(EventState.PENDING)
                .paid(false)
                .location(savedLocation)
                .build();
    }

    private User createUser(int id) {
        return User.builder()
                .name("username" + id)
                .email("user" + id + "@email.com")
                .build();
    }

    private ParticipationRequest createRequest(User requester, Event event) {
        return ParticipationRequest.builder()
                .status(ParticipationStatus.PENDING)
                .requester(requester)
                .event(event)
                .build();
    }
}