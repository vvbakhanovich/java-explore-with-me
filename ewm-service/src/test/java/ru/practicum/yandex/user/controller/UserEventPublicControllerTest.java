package ru.practicum.yandex.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.events.dto.CommentDto;
import ru.practicum.yandex.events.dto.CommentRequestDto;
import ru.practicum.yandex.events.dto.EventFullDto;
import ru.practicum.yandex.events.dto.EventShortDto;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.dto.LocationDto;
import ru.practicum.yandex.events.mapper.CommentMapper;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Comment;
import ru.practicum.yandex.events.model.CommentRequest;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.shared.exception.EventNotModifiableException;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.dto.UserShortDto;
import ru.practicum.yandex.user.mapper.ParticipationMapper;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserPrivateController.class)
class UserEventPublicControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private EventMapper eventMapper;

    @MockBean
    private ParticipationMapper participationMapper;

    @MockBean
    private CommentMapper commentMapper;

    private NewEventDto newEventDto;

    private NewEvent newEvent;

    private Event event;

    private EventFullDto eventFullDto;

    private EventShortDto eventShortDto;

    private Long userId;

    private Long eventId;

    @Value("${spring.jackson.date-format}")
    private String formatPattern;

    private EventUpdateRequest updateEvent;

    @BeforeEach
    void init() {
        LocationDto locationDto = new LocationDto(41.2F, 43.3F);
        newEventDto = NewEventDto.builder()
                .annotation("new event dto annotation")
                .description("new added event description")
                .eventDate(LocalDateTime.of(2028, 10, 11, 12, 43, 12))
                .participantLimit(213)
                .title("title")
                .paid(true)
                .categoryId(3L)
                .location(locationDto)
                .requestModeration(false)
                .build();
        newEvent = new NewEvent();
        CategoryDto categoryDto = CategoryDto.builder()
                .name("category name")
                .build();
        UserShortDto userShortDto = UserShortDto.builder()
                .name("username")
                .build();
        eventFullDto = EventFullDto.builder()
                .annotation("full event dto annotation")
                .description("event full dto description")
                .eventDate(LocalDateTime.of(2028, 10, 11, 12, 43, 12))
                .participantLimit(213)
                .title("title")
                .paid(true)
                .location(locationDto)
                .initiator(userShortDto)
                .category(categoryDto)
                .requestModeration(false)
                .build();
        event = new Event();
        eventShortDto = EventShortDto.builder()
                .annotation("short annotation")
                .title("short title")
                .eventDate(LocalDateTime.of(2028, 10, 11, 12, 43, 12))
                .paid(true)
                .category(categoryDto)
                .initiator(userShortDto)
                .build();
        userId = 1L;
        eventId = 2L;
        updateEvent = new EventUpdateRequest();
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user")
    void addEvent_whenAllFieldsValid_ShouldReturnStatus201() {
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event with max length annotation by user")
    void addEvent_whenMaxLengthAnnotation_ShouldReturnStatus201() {
        String annotation = "a".repeat(2000);
        newEventDto.setAnnotation(annotation);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event with min length annotation by user")
    void addEvent_whenMinLengthAnnotation_ShouldReturnStatus201() {
        String annotation = "a".repeat(20);
        newEventDto.setAnnotation(annotation);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with too short annotation")
    void addEvent_whenAnnotationIsTooShort_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        newEventDto.setAnnotation("annotation");
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: annotation. " +
                        "Error = Annotation must not be blank or empty and contain between 20 and 2000 characters. " +
                        "Value: annotation")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with too long annotation")
    void addEvent_whenAnnotationIsTooLong_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String annotation = "a".repeat(2001);
        newEventDto.setAnnotation(annotation);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: annotation. " +
                        "Error = Annotation must not be blank or empty and contain between 20 and 2000 characters. " +
                        "Value: " + annotation)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with null annotation")
    void addEvent_whenAnnotationIsNull_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String annotation = null;
        newEventDto.setAnnotation(annotation);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: annotation. " +
                        "Error = Annotation must not be blank or empty and contain between 20 and 2000 characters. " +
                        "Value: " + annotation)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with blank annotation")
    void addEvent_whenAnnotationIsBlank_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String annotation = "  ";
        newEventDto.setAnnotation(annotation);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: annotation. " +
                        "Error = Annotation must not be blank or empty and contain between 20 and 2000 characters. " +
                        "Value: " + annotation)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with null category id")
    void addEvent_whenCategoryIdIsNull_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        Long categoryId = null;
        newEventDto.setCategoryId(categoryId);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: categoryId. " +
                        "Error = Event must have category. " +
                        "Value: " + categoryId)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event with max length description by user")
    void addEvent_whenMaxLengthDescription_ShouldReturnStatus201() {
        String description = "a".repeat(7000);
        newEventDto.setDescription(description);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event with min length description by user")
    void addEvent_whenMinLengthDescription_ShouldReturnStatus201() {
        String description = "a".repeat(20);
        newEventDto.setDescription(description);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with too short description")
    void addEvent_whenDescriptionIsTooShort_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        newEventDto.setDescription("description");
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: description. " +
                        "Error = Description must not be blank or empty and contain between 20 and 7000 characters. " +
                        "Value: description")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with too long description")
    void addEvent_whenDescriptionIsTooLong_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String description = "a".repeat(7001);
        newEventDto.setDescription(description);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: description. " +
                        "Error = Description must not be blank or empty and contain between 20 and 7000 characters. " +
                        "Value: " + description)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with null description")
    void addEvent_whenDescriptionIsNull_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String description = null;
        newEventDto.setDescription(description);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: description. " +
                        "Error = Description must not be blank or empty and contain between 20 and 7000 characters. " +
                        "Value: " + description)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with blank description")
    void addEvent_whenDescriptionIsBlank_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String description = "  ";
        newEventDto.setDescription(description);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: description. " +
                        "Error = Description must not be blank or empty and contain between 20 and 7000 characters. " +
                        "Value: " + description)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event with not valid event date by user")
    void addEvent_whenInvalidEventDate_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        LocalDateTime eventDate = LocalDateTime.now();
        newEventDto.setEventDate(eventDate);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: eventDate. " +
                        "Error = Date of event must be at least 2 hours later than current time. " +
                        "Value: " + eventDate.truncatedTo(ChronoUnit.SECONDS))))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event with max length title by user")
    void addEvent_whenMaxLengthTitle_ShouldReturnStatus201() {
        String title = "a".repeat(120);
        newEventDto.setTitle(title);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event with min length title by user")
    void addEvent_whenMinLengthTitle_ShouldReturnStatus201() {
        String title = "a".repeat(3);
        newEventDto.setTitle(title);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with too short title")
    void addEvent_whenTitleIsTooShort_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String title = "a".repeat(2);
        newEventDto.setTitle(title);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title must not be blank or empty and contain between 3 and 120 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with too long title")
    void addEvent_whenTitleIsTooLong_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String title = "a".repeat(121);
        newEventDto.setTitle(title);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title must not be blank or empty and contain between 3 and 120 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with null title")
    void addEvent_whenTitleIsNull_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String title = null;
        newEventDto.setTitle(title);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title must not be blank or empty and contain between 3 and 120 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with blank title")
    void addEvent_whenTitleIsBlank_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String title = " ";
        newEventDto.setTitle(title);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title must not be blank or empty and contain between 3 and 120 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user without location")
    void addEvent_withoutLocationDto_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        LocationDto locationDto = null;
        newEventDto.setLocation(locationDto);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: location. " +
                        "Error = Location must be specified. " +
                        "Value: " + locationDto)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with negative participant limit")
    void addEvent_whenNegativeParticipantLimit_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        int participantLimit = -1;
        newEventDto.setParticipantLimit(participantLimit);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: participantLimit. " +
                        "Error = Number of participants must be positive or zero. " +
                        "Value: " + participantLimit)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(eventMapper, never()).toModel(any());
        verify(userService, never()).addEventByUser(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user with zero participant limit")
    void addEvent_whenZeroParticipantLimit_ShouldReturnStatus201() {
        int participantLimit = 0;
        newEventDto.setParticipantLimit(participantLimit);
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add event by user when service throws NotFoundException")
    void addEvent_whenServiceThrowsNotFoundException_ShouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        when(eventMapper.toModel(newEventDto))
                .thenReturn(newEvent);
        when(userService.addEventByUser(userId, newEvent))
                .thenThrow(new NotFoundException("User with id '" + userId + "' was not found."));
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' was not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));

        verify(eventMapper, times(1)).toModel(newEventDto);
        verify(userService, times(1)).addEventByUser(userId, newEvent);
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Find events from user")
    void findEventsFromUser_ShouldReturnListAndStatus200() {
        Long from = 0L;
        Integer size = 10;
        List<Event> events = List.of(event);
        when(userService.findEventsFromUser(userId, from, size))
                .thenReturn(events);
        List<EventShortDto> shortDtos = List.of(eventShortDto);
        when(eventMapper.toShortDtoList(events))
                .thenReturn(shortDtos);

        mvc.perform(get("/users/{userId}/events", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(events.size())))
                .andExpect(jsonPath("$.[0].id", is(shortDtos.get(0).getId())))
                .andExpect(jsonPath("$.[0].annotation", is(shortDtos.get(0).getAnnotation())))
                .andExpect(jsonPath("$.[0].eventDate", is(shortDtos.get(0).getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.[0].paid", is(shortDtos.get(0).getPaid())))
                .andExpect(jsonPath("$.[0].title", is(shortDtos.get(0).getTitle())))
                .andExpect(jsonPath("$.[0].category.id", is(shortDtos.get(0).getCategory().getId())))
                .andExpect(jsonPath("$.[0].category.name", is(shortDtos.get(0).getCategory().getName())))
                .andExpect(jsonPath("$.[0].initiator.id", is(shortDtos.get(0).getInitiator().getId())))
                .andExpect(jsonPath("$.[0].initiator.name", is(shortDtos.get(0).getInitiator().getName())))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(shortDtos.get(0).getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.[0].views", is(shortDtos.get(0).getViews()), Long.class));

        verify(userService, times(1)).findEventsFromUser(userId, from, size);
        verify(eventMapper, times(1)).toShortDtoList(events);
    }

    @Test
    @SneakyThrows
    @DisplayName("Find events from user when user not found")
    void findEventsFromUser_whenUserNotFound_ShouldThrowNotFoundExceptionAndReturn404Status() {
        Long from = 0L;
        Integer size = 10;
        List<Event> events = List.of(event);
        when(userService.findEventsFromUser(userId, from, size))
                .thenThrow(new NotFoundException("User with id '" + userId + "' was not found."));
        List<EventShortDto> shortDtos = List.of(eventShortDto);
        when(eventMapper.toShortDtoList(events))
                .thenReturn(shortDtos);

        mvc.perform(get("/users/{userId}/events", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' was not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));

        verify(userService, times(1)).findEventsFromUser(userId, from, size);
        verify(eventMapper, never()).toShortDtoList(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Get event by initiator")
    void getFullEventByInitiator_shouldReturnEventAndStatus200() {
        when(userService.getFullEventByInitiator(userId, eventId))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).getFullEventByInitiator(userId, eventId);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Get event by initiator not found")
    void getFullEventByInitiator_whenInitiatorNotFound_shouldThrowNotFoundExceptionAnd404Status() {
        when(userService.getFullEventByInitiator(userId, eventId))
                .thenThrow(new NotFoundException("User with id '" + userId + "' was not found."));

        mvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' was not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));

        verify(userService, times(1)).getFullEventByInitiator(userId, eventId);
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Get event by initiator, user not authorized")
    void getFullEventByInitiator_whenUserNotAuthorized_shouldThrowNotAuthorizedExceptionAnd404Status() {
        when(userService.getFullEventByInitiator(userId, eventId))
                .thenThrow(new NotAuthorizedException("User with id '" + userId + "' is not an initiator of event with id '" +
                        event.getId() + "'."));

        mvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' is not an initiator of event with id '" +
                        event.getId() + "'.")))
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated.")));

        verify(userService, times(1)).getFullEventByInitiator(userId, eventId);
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event without any fields")
    void updateEvent_shouldReturnEventAndStatus200() {
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with min length annotation")
    void updateEvent_withMinLengthAnnotation_shouldReturnEventAndStatus200() {
        updateEvent.setAnnotation("a".repeat(20));
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with max length annotation")
    void updateEvent_withMaxLengthAnnotation_shouldReturnEventAndStatus200() {
        updateEvent.setAnnotation("a".repeat(2000));
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with less than min length annotation")
    void updateEvent_withLessThenMinLengthAnnotation_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String annotation = "a".repeat(19);
        updateEvent.setAnnotation(annotation);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: annotation. " +
                        "Error = Annotation must not be blank or empty and contain between 20 and 2000 characters. " +
                        "Value: " + annotation)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with more than max length annotation")
    void updateEvent_withMoreThenMaxLengthAnnotation_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String annotation = "a".repeat(2001);
        updateEvent.setAnnotation(annotation);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: annotation. " +
                        "Error = Annotation must not be blank or empty and contain between 20 and 2000 characters. " +
                        "Value: " + annotation)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with negative category id")
    void updateEvent_whenNegativeCategoryId_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        Long categoryId = -1L;
        updateEvent.setCategoryId(categoryId);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: categoryId. " +
                        "Error = Category id must be positive. " +
                        "Value: " + categoryId)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with zero category id")
    void updateEvent_whenCategoryIdIsZero_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        Long categoryId = 0L;
        updateEvent.setCategoryId(categoryId);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: categoryId. " +
                        "Error = Category id must be positive. " +
                        "Value: " + categoryId)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with positive category id")
    void updateEvent_whenCategoryIdIsPositive_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        Long categoryId = 4L;
        updateEvent.setCategoryId(categoryId);
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with min length description")
    void updateEvent_withMinLengthDescription_shouldReturnEventAndStatus200() {
        updateEvent.setDescription("a".repeat(20));
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with max length description")
    void updateEvent_withMaxLengthDescription_shouldReturnEventAndStatus200() {
        updateEvent.setDescription("a".repeat(7000));
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with less than min length description")
    void updateEvent_withLessThenMinLengthDescription_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String description = "a".repeat(19);
        updateEvent.setDescription(description);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: description. " +
                        "Error = Description must not be blank or empty and contain between 20 and 7000 characters. " +
                        "Value: " + description)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with more than max length description")
    void updateEvent_withMoreThenMaxLengthDescription_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String description = "a".repeat(7001);
        updateEvent.setDescription(description);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: description. " +
                        "Error = Description must not be blank or empty and contain between 20 and 7000 characters. " +
                        "Value: " + description)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with invalid event date")
    void updateEvent_withInvalidEventDAte_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        LocalDateTime eventDate = LocalDateTime.now();
        updateEvent.setEventDate(eventDate);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: eventDate. " +
                        "Error = Date of event must be at least 2 hours later than current time. " +
                        "Value: " + eventDate.truncatedTo(ChronoUnit.SECONDS))))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with min length title")
    void updateEvent_withMinLengthTitle_shouldReturnEventAndStatus200() {
        updateEvent.setTitle("a".repeat(3));
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with max length title")
    void updateEvent_withMaxLengthTitle_shouldReturnEventAndStatus200() {
        updateEvent.setTitle("a".repeat(120));
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with less than min length title")
    void updateEvent_withLessThenMinLengthTitle_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String title = "a".repeat(2);
        updateEvent.setTitle(title);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title must not be blank or empty and contain between 3 and 120 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with more than max length title")
    void updateEvent_withMoreThenMaxLengthTitle_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        String title = "a".repeat(7001);
        updateEvent.setTitle(title);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title must not be blank or empty and contain between 3 and 120 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with negative participant limit")
    void updateEvent_whenNegativeParticipantLimit_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        Integer participantLimit = -1;
        updateEvent.setParticipantLimit(participantLimit);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: participantLimit. " +
                        "Error = Participant limit must be positive or zero. " +
                        "Value: " + participantLimit)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(userService, never()).updateEvent(any(), any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with zero participant limit")
    void updateEvent_whenParticipantLimitIdIsZero_shouldReturn200Status() {
        Integer participantLimit = 0;
        updateEvent.setParticipantLimit(participantLimit);
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event with positive participant limit")
    void updateEvent_whenParticipantLimitIsPositive_shouldThrowMethodArgumentNotValidExceptionAndReturn400Status() {
        Integer participantLimit = 3;
        updateEvent.setParticipantLimit(participantLimit);

        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenReturn(event);
        when(eventMapper.toDto(event))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Float.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Float.class))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId())))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName())))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId())))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getNumberOfParticipants())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit())));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event when user not found")
    void updateEvent_whenUserNotFound_shouldNotFoundExceptionAndReturn404Status() {
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenThrow(new NotFoundException("User with id '" + userId + "' was not found."));

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' was not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update published event")
    void updateEvent_whenEventIsPublished_shouldEventNotModifiableExceptionAndReturn409Status() {
        when(userService.updateEvent(userId, eventId, updateEvent))
                .thenThrow(new EventNotModifiableException("Published event with id '" + eventId + "' can not be modified."));

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEvent)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotModifiableException))
                .andExpect(jsonPath("$.message", is("Published event with id '" + eventId + "' can not be modified.")))
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated.")));

        verify(userService, times(1)).updateEvent(userId, eventId, updateEvent);
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add comment")
    void addCommentToEvent_shouldReturn201Status() {
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .text("comment")
                .build();
        Comment comment = new Comment();
        CommentDto commentDto = CommentDto.builder()
                .text("commentDto")
                .build();
        when(commentMapper.toModel(commentRequestDto))
                .thenReturn(comment);
        when(userService.addCommentToEvent(userId, eventId, comment))
                .thenReturn(comment);
        when(commentMapper.toDto(comment))
                .thenReturn(commentDto);

        mvc.perform(post("/users/{userId}/events/{eventId}/comments", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(commentDto.getText())));

        verify(commentMapper, times(1)).toModel(commentRequestDto);
        verify(userService, times(1)).addCommentToEvent(userId, eventId, comment);
        verify(commentMapper, times(1)).toDto(comment);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add empty comment")
    void addCommentToEvent_whenEmptyComment_shouldReturn400Status() {
        String text = "";
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .text(text)
                .build();


        mvc.perform(post("/users/{userId}/events/{eventId}/comments", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: text. " +
                        "Error = Text must not be blank or empty and must have between 1 and 2000 characters. " +
                        "Value: " + text)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(commentMapper, never()).toModel(any());
        verify(userService, never()).addCommentToEvent(any(), any(), any());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add comment with too long text")
    void addCommentToEvent_whenTooLongComment_shouldReturn400Status() {
        String text = "a".repeat(2001);
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .text(text)
                .build();


        mvc.perform(post("/users/{userId}/events/{eventId}/comments", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: text. " +
                        "Error = Text must not be blank or empty and must have between 1 and 2000 characters. " +
                        "Value: " + text)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(commentMapper, never()).toModel(any());
        verify(userService, never()).addCommentToEvent(any(), any(), any());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update comment")
    void updateComment_shouldReturn200Status() {
        Long commentId = 3L;
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .text("comment")
                .build();
        CommentRequest commentRequest = new CommentRequest();
        Comment comment = new Comment();
        CommentDto commentDto = CommentDto.builder()
                .text("commentDto")
                .build();
        when(commentMapper.toRequestModel(commentRequestDto))
                .thenReturn(commentRequest);
        when(userService.updateComment(userId, commentId, commentRequest))
                .thenReturn(comment);
        when(commentMapper.toDto(comment))
                .thenReturn(commentDto);

        mvc.perform(patch("/users/{userId}/comments/{commentId}", userId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(commentDto.getText())));

        verify(commentMapper, times(1)).toRequestModel(commentRequestDto);
        verify(userService, times(1)).updateComment(userId, commentId, commentRequest);
        verify(commentMapper, times(1)).toDto(comment);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update empty comment")
    void updateComment_whenEmptyComment_shouldReturn400Status() {
        Long commentId = 3L;
        String text = "";
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .text(text)
                .build();

        mvc.perform(patch("/users/{userId}/comments/{commentId}", userId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: text. " +
                        "Error = Text must not be blank or empty and must have between 1 and 2000 characters. " +
                        "Value: " + text)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(commentMapper, never()).toModel(any());
        verify(userService, never()).updateComment(any(), any(), any());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update comment with too long text")
    void updateComment_whenTooLongComment_shouldReturn400Status() {
        Long commentId = 3L;
        String text = "a".repeat(2001);
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .text(text)
                .build();

        mvc.perform(patch("/users/{userId}/comments/{commentId}", userId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: text. " +
                        "Error = Text must not be blank or empty and must have between 1 and 2000 characters. " +
                        "Value: " + text)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(commentMapper, never()).toModel(any());
        verify(userService, never()).updateComment(any(), any(), any());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Delete comment")
    void deleteComment_shouldReturn204Status() {
        Long commentId = 3L;

        mvc.perform(delete("/users/{userId}/comments/{commentId}", userId, commentId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteComment(userId, commentId);
    }
}
