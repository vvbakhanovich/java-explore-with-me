package ru.practicum.yandex.events.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.service.CategoryService;
import ru.practicum.yandex.events.dto.EventAdminSearchFilter;
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.events.dto.EventSort;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.model.Comment;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;
import ru.practicum.yandex.events.model.Location;
import ru.practicum.yandex.events.repository.CommentRepository;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.dto.StateAction;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class EventServiceImplTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CommentRepository commentRepository;

    private Event savedEvent1;

    private Event savedEvent2;

    private User savedUser1;

    private User savedUser2;

    private EventSearchFilter searchFilter;

    private Category savedCategory1;

    private Category savedCategory2;

    Long unknownId;

    @BeforeEach
    void init() {
        Location location = Location.builder()
                .lat(-414.43F)
                .lon(43.43F)
                .build();
        Category category1 = Category.builder()
                .name("category1")
                .build();
        savedCategory1 = categoryService.addCategory(category1);
        Category category2 = Category.builder()
                .name("category2")
                .build();
        savedCategory2 = categoryService.addCategory(category2);
        User user1 = User.builder()
                .name("name1")
                .email("user1@email.com")
                .build();
        savedUser1 = userService.createUser(user1);
        User user2 = User.builder()
                .name("name2")
                .email("user2@email.com")
                .build();
        savedUser2 = userService.createUser(user2);
        NewEvent newEvent1 = NewEvent.builder()
                .annotation("annotation1")
                .description("description1")
                .eventDate(LocalDateTime.of(2025, 10, 11, 12, 23, 11))
                .participantLimit(134)
                .requestModeration(false)
                .title("title")
                .paid(false)
                .location(location)
                .categoryId(savedCategory1.getId())
                .build();
        NewEvent newEvent2 = NewEvent.builder()
                .annotation("annotation2")
                .description("description2")
                .eventDate(LocalDateTime.of(2025, 10, 11, 12, 23, 11))
                .participantLimit(1)
                .requestModeration(false)
                .title("title")
                .paid(true)
                .location(location)
                .categoryId(savedCategory2.getId())
                .build();
        savedEvent1 = userService.addEventByUser(savedUser1.getId(), newEvent1);
        savedEvent2 = userService.addEventByUser(savedUser1.getId(), newEvent2);
        unknownId = 999L;
    }

    @Test
    @DisplayName("Search by text")
    void findEvents_whenSearchByText_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by text ignore case")
    void findEvents_whenSearchByTextIgnoreCase_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("NNOTaT")
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search several events by text ignore case")
    void findEvents_whenSearchSeveralEventsByTextIgnoreCase_shouldReturnEvents() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        eventService.updateEventByAdmin(savedEvent2.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("NNOTaT")
                .sort(EventSort.EVENT_DATE)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
        assertThat(events.get(1).getId(), is(savedEvent2.getId()));
    }

    @Test
    @DisplayName("Search several events by text ignore case")
    void findEvents_whenSearchSeveralEventsByTextIgnoreCaseOrderByViews_shouldReturnEvents() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        eventService.updateEventByAdmin(savedEvent2.getId(), updateRequest);
        eventService.getFullEventInfoById(savedEvent2.getId(), 2L);
        searchFilter = EventSearchFilter.builder()
                .text("NNOTaT")
                .sort(EventSort.VIEWS)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getId(), is(savedEvent2.getId()));
        assertThat(events.get(1).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search several events by text ignore case")
    void findEvents_whenSearchSeveralEventsByTextIgnoreCase_shouldReturnOneEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        eventService.updateEventByAdmin(savedEvent2.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("1")
                .sort(EventSort.EVENT_DATE)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by paid = false")
    void findEvents_whenSearchByPaid_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .paid(false)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by paid = true")
    void findEvents_whenSearchByPaidTrue_shouldEmptyList() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .paid(true)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events, emptyIterable());
    }

    @Test
    @DisplayName("Search by category")
    void findEvents_whenSearchByCategory_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .categories(List.of(savedCategory1.getId()))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by multiple categories")
    void findEvents_whenSearchByMultipleCategory_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .categories(List.of(savedCategory1.getId(), 99L, 344L))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by date range")
    void findEvents_whenSearchByDateRange_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12, 23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12, 23, 11))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by out of date range")
    void findEvents_whenSearchByOutOfDateRange_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .rangeStart(LocalDateTime.of(2023, 10, 11, 12, 23, 11))
                .rangeEnd(LocalDateTime.of(2024, 10, 11, 12, 23, 11))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events, emptyIterable());
    }

    @Test
    @DisplayName("Search by available")
    void findEvents_whenSearchByOnlyAvailable_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .onlyAvailable(true)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }


    @Test
    @DisplayName("Search by available")
    void findEvents_whenEventIsNotPublished_shouldReturnEmptyList() {
        searchFilter = EventSearchFilter.builder()
                .onlyAvailable(true)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events, emptyIterable());
    }

    @Test
    @DisplayName("Search by text and category")
    void findEvents_whenSearchByTextAndCategory_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory1.getId()))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid")
    void findEvents_whenSearchByTextCategoryAndPaid_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory1.getId()))
                .paid(false)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range")
    void findEvents_whenSearchByTextCategoryPaidAndDateRange_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory1.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12, 23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12, 23, 11))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range, available")
    void findEvents_whenSearchByTextCategoryPaidDateRangeAndAvailable_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory1.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12, 23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12, 23, 11))
                .onlyAvailable(true)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range, available with sort by date")
    void findEvents_whenSearchByTextCategoryPaidDateRangeAndAvailableWithSortByDate_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory1.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12, 23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12, 23, 11))
                .onlyAvailable(true)
                .sort(EventSort.EVENT_DATE)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range, available with sort by date")
    void findEvents_whenSearchByTextCategoryPaidDateRangeAndAvailableWithSortByViews_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory1.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12, 23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12, 23, 11))
                .onlyAvailable(true)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range, available with sort by date")
    void findEvents_whenSearchByLongTextCategoryPaidDateRangeAndAvailableWithSortByViews_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .annotation("Ducimus aut nihil praesentium officia. Exercitationem voluptates sint incidunt quia voluptas itaque itaque commodi." +
                        " Facilis quis vero voluptas adipisci et est quia. Repudiandae qui vero quisquam.")
                .description("Vitae reiciendis corporis quia. In non quae quidem. Deleniti earum vel.\n" +
                        " \n" +
                        "Suscipit aut sit. Voluptas laboriosam sed architecto. At quae veritatis in.\n" +
                        " \n" +
                        "Nulla eius voluptatem aut odit. Ad voluptatem sint. Nostrum hic consequatur voluptatem aperiam aut. Sint autem maiores error et quas qui tempora animi omnis. Debitis aut consequuntur fugit quasi molestiae iste occaecati eaque perspiciatis.")
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("Ducimus aut nihil praesentium officia. Exercitationem voluptates sint incidunt quia voluptas itaque itaque commodi. Facilis quis vero voluptas adipisci et est quia. Repudiandae qui vero quisquam.")
                .categories(List.of(savedCategory1.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12, 23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12, 23, 11))
                .onlyAvailable(true)
                .sort(EventSort.VIEWS)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Get event by id")
    void getFullEventInfoById_whenEventExists_shouldReturnEventWithViews() {
        long views = 10L;
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);

        Event event = eventService.getFullEventInfoById(savedEvent1.getId(), views);

        assertThat(event, notNullValue());
        assertThat(event.getId(), is(savedEvent1.getId()));
        assertThat(event.getViews(), is(views));
    }

    @Test
    @DisplayName("Get not published event by id")
    void getFullEventInfoById_whenEventIsNotPublished_shouldThrowNotFoundException() {
        long views = 10L;
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> eventService.getFullEventInfoById(savedEvent1.getId(), views));

        assertThat(e.getMessage(), is("Event with id '" + savedEvent1.getId() + "' is not published. State: '"
                + savedEvent1.getState() + "'"));
    }

    @Test
    @DisplayName("Get not non existing event")
    void getFullEventInfoById_whenEventIsNotExists_shouldThrowNotFoundException() {
        long views = 10L;
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> eventService.getFullEventInfoById(999L, views));

        assertThat(e.getMessage(), is("Event with id '" + 999L + "' was not found."));
    }

    @Test
    @DisplayName("Get events by admin without filter")
    void getFullEventsInfoByAdmin_whenSearchFilterIsEmpty_shouldReturnAllEventsUnsorted() {
        EventAdminSearchFilter searchFilter = EventAdminSearchFilter.builder()
                .build();

        List<Event> events = eventService.getFullEventsInfoByAdmin(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
        assertThat(events.get(1).getId(), is(savedEvent2.getId()));
    }

    @Test
    @DisplayName("Get only published events by admin")
    void getFullEventsInfoByAdmin_whenSearchByPublished_shouldReturnPublishedEventsUnsorted() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);
        EventAdminSearchFilter searchFilter = EventAdminSearchFilter.builder()
                .states(List.of(EventState.PUBLISHED))
                .build();

        List<Event> events = eventService.getFullEventsInfoByAdmin(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Get only available events by admin")
    void getFullEventsInfoByAdmin_whenSearchByAvailable_shouldReturnNotPaidEventsUnsorted() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent2.getId(), updateRequest);
        userService.addParticipationRequestToEvent(savedUser2.getId(), savedEvent2.getId());
        EventAdminSearchFilter searchFilter = EventAdminSearchFilter.builder()
                .onlyAvailable(true)
                .build();

        List<Event> events = eventService.getFullEventsInfoByAdmin(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Get events by admin search by category")
    void getFullEventsInfoByAdmin_whenSearchByCategory_shouldReturnNotPaidEventsUnsorted() {
        EventAdminSearchFilter searchFilter = EventAdminSearchFilter.builder()
                .categories(List.of(savedCategory1.getId()))
                .build();

        List<Event> events = eventService.getFullEventsInfoByAdmin(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Publish event")
    void updateEventByAdmin_whenPublishRequest_shouldSetEventStateToPublish() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();

        Event updatedEvent = eventService.updateEventByAdmin(savedEvent1.getId(), updateRequest);

        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getState(), is(EventState.PUBLISHED));
        assertThat(updatedEvent.getTitle(), is(savedEvent1.getTitle()));
        assertThat(updatedEvent.getDescription(), is(savedEvent1.getDescription()));
        assertThat(updatedEvent.getAnnotation(), is(savedEvent1.getAnnotation()));
        assertThat(updatedEvent.isRequestModeration(), is(savedEvent1.isRequestModeration()));
    }

    @Test
    @DisplayName("Add comment")
    void addCommentToEvent_shouldReturnCommentWithNotNullId() {
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        eventService.addCommentToEvent(savedUser1.getId(), savedEvent1.getId(), comment);

        Event event = userService.getFullEventByInitiator(savedUser1.getId(), savedEvent1.getId());
        assertThat(event.getComments(), notNullValue());
        assertThat(event.getComments().size(), is(1));
        assertThat(event.getComments().get(0).getText(), is(comment.getText()));
    }

    @Test
    @DisplayName("Add comment to event by not existing user")
    void addCommentToEvent_whenUserNotFound_shouldThrowNotFoundException() {
        Comment comment = Comment.builder()
                .text("comment")
                .build();

        NotFoundException e = assertThrows(NotFoundException.class, () -> eventService
                .addCommentToEvent(unknownId, savedEvent1.getId(), comment));

        assertThat(e.getMessage(), is("User with id '" + unknownId + "' not found."));
    }

    @Test
    @DisplayName("Update comment")
    void updateComment_shouldReturnCommentWithUpdatedText() {
        Comment comment = Comment.builder()
                .text("comment")
                .build();

        Event commentedEvent = eventService.addCommentToEvent(savedUser1.getId(), savedEvent1.getId(), comment);
        Comment addedComment = commentedEvent.getComments().get(0);

        Comment updateComment = Comment.builder()
                .id(addedComment.getId())
                .text("updated comment")
                .build();
        Event eventWithUpdatedComment = eventService.updateComment(savedUser1.getId(), savedEvent1.getId(), updateComment);

        assertThat(eventWithUpdatedComment, notNullValue());
        assertThat(eventWithUpdatedComment.getComments(), notNullValue());
        assertThat(eventWithUpdatedComment.getComments().size(), is(1));
        assertThat(eventWithUpdatedComment.getComments().get(0).getId(), is(addedComment.getId()));
        assertThat(eventWithUpdatedComment.getComments().get(0).getText(), is(updateComment.getText()));
    }

    @Test
    @DisplayName("Update comment by not author")
    void updateComment_whenNotAuthorTryToUpdate_shouldThrowNotAuthorizedException() {
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Event commentedEvent = eventService.addCommentToEvent(savedUser1.getId(), savedEvent1.getId(), comment);
        Comment addedComment = commentedEvent.getComments().get(0);
        Comment updateComment = Comment.builder()
                .id(addedComment.getId())
                .text("updated comment")
                .build();

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class, () -> eventService
                .updateComment(savedUser2.getId(), addedComment.getId(), updateComment));

        assertThat(e.getMessage(), is("User with id '" + savedUser2.getId() + "' is not author of comment with id '" +
                addedComment.getId() + "'."));
    }

    @Test
    @DisplayName("Delete comment")
    void deleteComment_shouldRemoveCommentFromDb() {
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Event commentedEvent = eventService.addCommentToEvent(savedUser1.getId(), savedEvent1.getId(), comment);
        Comment addedComment = commentedEvent.getComments().get(0);

        eventService.deleteComment(savedUser1.getId(), addedComment.getId());

        Optional<Comment> optionalComment = commentRepository.findById(addedComment.getId());

        assertTrue(optionalComment.isEmpty());
    }

    @Test
    @DisplayName("Delete comment by not author")
    void deleteComment_whenNotAuthorTryToDelete_shouldThrowNotAuthorizedException() {
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Event commentedEvent = eventService.addCommentToEvent(savedUser1.getId(), savedEvent1.getId(), comment);
        Comment addedComment = commentedEvent.getComments().get(0);

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class, () -> eventService
                .deleteComment(savedUser2.getId(), addedComment.getId()));

        assertThat(e.getMessage(), is("User with id '" + savedUser2.getId() + "' is not author of comment with id '" +
                addedComment.getId() + "'."));
    }
}