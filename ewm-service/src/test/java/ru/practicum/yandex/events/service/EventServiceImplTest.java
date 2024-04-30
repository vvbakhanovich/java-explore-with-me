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
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.events.dto.EventSort;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.Location;
import ru.practicum.yandex.user.dto.StateAction;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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

    private Event savedEvent;

    private User savedUser;

    private EventSearchFilter searchFilter;

    private Category savedCategory;

    @BeforeEach
    void init() {
        Location location = Location.builder()
                .lat(-414.43F)
                .lon(43.43F)
                .build();
        Category category = Category.builder()
                .name("category")
                .build();
        savedCategory = categoryService.addCategory(category);
        User user1 = User.builder()
                .name("name")
                .email("user@email.com")
                .build();
        savedUser = userService.createUser(user1);
        NewEvent newEvent = NewEvent.builder()
                .annotation("annotation")
                .description("description")
                .eventDate(LocalDateTime.of(2025, 10, 11, 12,23, 11))
                .participantLimit(134)
                .requestModeration(false)
                .title("title")
                .paid(false)
                .location(location)
                .categoryId(savedCategory.getId())
                .build();
        savedEvent = userService.addEventByUser(savedUser.getId(), newEvent);
    }

    @Test
    @DisplayName("Search by text")
    void findEvents_whenSearchByText_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by text ignore case")
    void findEvents_whenSearchByTextIgnoreCase_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("NNOTaT")
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by paid")
    void findEvents_whenSearchByPaid_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .paid(false)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by paid = true")
    void findEvents_whenSearchByPaidTrue_shouldEmptyList() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
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
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .categories(List.of(savedCategory.getId()))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by multiple categories")
    void findEvents_whenSearchByMultipleCategory_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .categories(List.of(savedCategory.getId(), 99L, 344L))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by date range")
    void findEvents_whenSearchByDateRange_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12,23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12,23, 11))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by out of date range")
    void findEvents_whenSearchByOutOfDateRange_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .rangeStart(LocalDateTime.of(2023, 10, 11, 12,23, 11))
                .rangeEnd(LocalDateTime.of(2024, 10, 11, 12,23, 11))
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
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .onlyAvailable(true)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
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
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory.getId()))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid")
    void findEvents_whenSearchByTextCategoryAndPaid_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory.getId()))
                .paid(false)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range")
    void findEvents_whenSearchByTextCategoryPaidAndDateRange_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12,23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12,23, 11))
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range, available")
    void findEvents_whenSearchByTextCategoryPaidDateRangeAndAvailable_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12,23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12,23, 11))
                .onlyAvailable(true)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range, available with sort by date")
    void findEvents_whenSearchByTextCategoryPaidDateRangeAndAvailableWithSortByDate_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12,23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12,23, 11))
                .onlyAvailable(true)
                .sort(EventSort.EVENT_DATE)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    @DisplayName("Search by text, category, paid, date range, available with sort by date")
    void findEvents_whenSearchByTextCategoryPaidDateRangeAndAvailableWithSortByViews_shouldReturnEvent() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("annot")
                .categories(List.of(savedCategory.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12,23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12,23, 11))
                .onlyAvailable(true)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
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
        eventService.updateEventByAdmin(savedEvent.getId(), updateRequest);
        searchFilter = EventSearchFilter.builder()
                .text("Ducimus aut nihil praesentium officia. Exercitationem voluptates sint incidunt quia voluptas itaque itaque commodi. Facilis quis vero voluptas adipisci et est quia. Repudiandae qui vero quisquam.")
                .categories(List.of(savedCategory.getId()))
                .paid(false)
                .rangeStart(LocalDateTime.of(2024, 10, 11, 12,23, 11))
                .rangeEnd(LocalDateTime.of(2026, 10, 11, 12,23, 11))
                .onlyAvailable(true)
                .sort(EventSort.VIEWS)
                .build();

        List<Event> events = eventService.findEvents(searchFilter, 0L, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(savedEvent.getId()));
    }

    @Test
    void getFullEventInfoById() {
    }

    @Test
    void getFullEventsInfoByAdmin() {
    }

    @Test
    void updateEventByAdmin() {
    }
}