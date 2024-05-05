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
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.shared.exception.EventNotModifiableException;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.shared.exception.RequestAlreadyExistsException;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateDto;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.yandex.user.dto.ParticipationRequestDto;
import ru.practicum.yandex.user.mapper.ParticipationMapper;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.model.ParticipationStatus;
import ru.practicum.yandex.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserEventRequestControllerTest {

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

    private ParticipationRequestDto participationRequestDto;

    private ParticipationRequest participationRequest;

    private Event event;

    private Long userId;

    private Long eventId;

    @Value("${spring.jackson.date-format}")
    private String formatPattern;

    @BeforeEach
    void init() {
        participationRequestDto = ParticipationRequestDto.builder()
                .requester(userId)
                .event(eventId)
                .status(ParticipationStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
        participationRequest = new ParticipationRequest();
        userId = 1L;
        eventId = 2L;
        event = new Event();
    }

    @Test
    @SneakyThrows
    @DisplayName("Add participation request")
    void addParticipationRequestToEvent_shouldReturn201Status() {
        when(userService.addParticipationRequestToEvent(userId, eventId))
                .thenReturn(participationRequest);
        when(participationMapper.toDto(participationRequest))
                .thenReturn(participationRequestDto);

        mvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(participationRequestDto.getId())))
                .andExpect(jsonPath("$.created", is(participationRequestDto.getCreated()
                        .format(DateTimeFormatter.ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.status", is(participationRequestDto.getStatus().toString())))
                .andExpect(jsonPath("$.event", is(participationRequestDto.getEvent())))
                .andExpect(jsonPath("$.requester", is(participationRequestDto.getRequester())));

        verify(userService, times(1)).addParticipationRequestToEvent(userId, eventId);
        verify(participationMapper, times(1)).toDto(participationRequest);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add participation request, user not found")
    void addParticipationRequestToEvent_whenUserNotFound_shouldThrowNotFoundExceptionAndReturn404Status() {
        when(userService.addParticipationRequestToEvent(userId, eventId))
                .thenThrow(new NotFoundException("User with id '" + userId + "' not found."));

        mvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));

        verify(userService, times(1)).addParticipationRequestToEvent(userId, eventId);
        verify(participationMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add participation request, user cant not make request")
    void addParticipationRequestToEvent_whenUserCanNotMakeRequest_shouldThrowNotFoundExceptionAndReturn404Status() {
        when(userService.addParticipationRequestToEvent(userId, eventId))
                .thenThrow(new NotAuthorizedException("Initiator with id '" + userId + "' can not make participation request " +
                        "to his own event with id '" + eventId + "'."));

        mvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.message", is("Initiator with id '" + userId + "' can not make participation request " +
                        "to his own event with id '" + eventId + "'.")))
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated.")));

        verify(userService, times(1)).addParticipationRequestToEvent(userId, eventId);
        verify(participationMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add participation request, participation already exists")
    void addParticipationRequestToEvent_whenParticipationAlreadyExists_shouldThrowNotFoundExceptionAndReturn404Status() {
        when(userService.addParticipationRequestToEvent(userId, eventId))
                .thenThrow(new RequestAlreadyExistsException("Participation request by user with id '" + userId + "' to event " +
                        "with id '" + eventId + "' already exists."));

        mvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RequestAlreadyExistsException))
                .andExpect(jsonPath("$.message", is("Participation request by user with id '" + userId + "' to event " +
                        "with id '" + eventId + "' already exists.")))
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated.")));

        verify(userService, times(1)).addParticipationRequestToEvent(userId, eventId);
        verify(participationMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Find participation request by user")
    void findParticipationRequestsByUser_shouldReturnListAnd200Status() {
        when(userService.findParticipationRequestsByUser(userId))
                .thenReturn(List.of(participationRequest));
        List<ParticipationRequestDto> requestDtos = List.of(participationRequestDto);
        when(participationMapper.toDtoList(List.of(participationRequest)))
                .thenReturn(requestDtos);

        mvc.perform(get("/users/{userId}/requests", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(requestDtos.size())))
                .andExpect(jsonPath("$.[0].id", is(requestDtos.get(0).getId())))
                .andExpect(jsonPath("$.[0].created", is(requestDtos.get(0).getCreated()
                        .format(DateTimeFormatter.ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.[0].status", is(requestDtos.get(0).getStatus().toString())))
                .andExpect(jsonPath("$.[0].event", is(requestDtos.get(0).getEvent())))
                .andExpect(jsonPath("$.[0].requester", is(requestDtos.get(0).getRequester())));

        verify(userService, times(1)).findParticipationRequestsByUser(userId);
        verify(participationMapper, times(1)).toDtoList(List.of(participationRequest));
    }

    @Test
    @SneakyThrows
    @DisplayName("Cancel participation request")
    void cancelOwnParticipationRequest_shouldReturn200Status() {
        Long requestId = 4L;
        when(userService.cancelOwnParticipationRequest(userId, requestId))
                .thenReturn(participationRequest);
        when(participationMapper.toDto(participationRequest))
                .thenReturn(participationRequestDto);

        mvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(participationRequestDto.getId())))
                .andExpect(jsonPath("$.created", is(participationRequestDto.getCreated()
                        .format(DateTimeFormatter.ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.status", is(participationRequestDto.getStatus().toString())))
                .andExpect(jsonPath("$.event", is(participationRequestDto.getEvent())))
                .andExpect(jsonPath("$.requester", is(participationRequestDto.getRequester())));

        verify(userService, times(1)).cancelOwnParticipationRequest(userId, requestId);
        verify(participationMapper, times(1)).toDto(participationRequest);
    }

    @Test
    @SneakyThrows
    @DisplayName("Cancel participation request, participation request not found")
    void cancelOwnParticipationRequest_whenParticipationRequestNotFound_shouldThrowNotFoundExceptionAndReturn404Status() {
        Long requestId = 4L;
        when(userService.cancelOwnParticipationRequest(userId, requestId))
                .thenThrow(new NotFoundException("Participation request with id '" + requestId + "' was not found."));
        mvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("Participation request with id '" + requestId + "' was not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));
        verify(userService, times(1)).cancelOwnParticipationRequest(userId, requestId);
        verify(participationMapper, never()).toDto(participationRequest);
    }

    @Test
    @SneakyThrows
    @DisplayName("Cancel participation request, user is not authorized to cancel request")
    void cancelOwnParticipationRequest_whenUserNotAuthorizedToCancleRequest_shouldThrowNotAuthorizedExceptionAndReturn409Status() {
        Long requestId = 4L;
        when(userService.cancelOwnParticipationRequest(userId, requestId))
                .thenThrow(new NotAuthorizedException("User with id '" + userId + "' is not authorized to cancel participation request with" +
                        "id '" + participationRequest.getId() + "'."));
        mvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' is not authorized to cancel participation request with" +
                        "id '" + participationRequest.getId() + "'.")))
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated.")));
        verify(userService, times(1)).cancelOwnParticipationRequest(userId, requestId);
        verify(participationMapper, never()).toDto(participationRequest);
    }

    @Test
    @SneakyThrows
    @DisplayName("Find participation requests for user's event")
    void findParticipationRequestsForUsersEvent_shouldReturnListAnd200Status() {
        when(userService.findParticipationRequestsForUsersEvent(userId, eventId))
                .thenReturn(List.of(participationRequest));
        List<ParticipationRequestDto> requestDtos = List.of(participationRequestDto);
        when(participationMapper.toDtoList(List.of(participationRequest)))
                .thenReturn(requestDtos);

        mvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(requestDtos.size())))
                .andExpect(jsonPath("$.[0].id", is(requestDtos.get(0).getId())))
                .andExpect(jsonPath("$.[0].created", is(requestDtos.get(0).getCreated()
                        .format(DateTimeFormatter.ofPattern(formatPattern)))))
                .andExpect(jsonPath("$.[0].status", is(requestDtos.get(0).getStatus().toString())))
                .andExpect(jsonPath("$.[0].event", is(requestDtos.get(0).getEvent())))
                .andExpect(jsonPath("$.[0].requester", is(requestDtos.get(0).getRequester())));

        verify(userService, times(1)).findParticipationRequestsForUsersEvent(userId, eventId);
        verify(participationMapper, times(1)).toDtoList(List.of(participationRequest));
    }

    @Test
    @SneakyThrows
    @DisplayName("Find participation requests for user's event, requester not found")
    void findParticipationRequestsForUsersEvent_whenRequesterNotFound_shouldThrowNotFoundExceptionAnd404Status() {
        when(userService.findParticipationRequestsForUsersEvent(userId, eventId))
                .thenThrow(new NotFoundException("User with id '" + userId + "' not found."));

        mvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));

        verify(userService, times(1)).findParticipationRequestsForUsersEvent(userId, eventId);
        verify(participationMapper, never()).toDtoList(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Find participation requests for user's event, requester is not an inititator")
    void findParticipationRequestsForUsersEvent_whenRequesterIsNotInitiator_shouldThrowNotAuthorizedExceptionAnd409Status() {
        when(userService.findParticipationRequestsForUsersEvent(userId, eventId))
                .thenThrow(new NotAuthorizedException("User with id '" + userId + "' is not an initiator of event with id '" +
                        event.getId() + "'."));

        mvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' is not an initiator of event with id '" +
                        event.getId() + "'.")))
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated.")));

        verify(userService, times(1)).findParticipationRequestsForUsersEvent(userId, eventId);
        verify(participationMapper, never()).toDtoList(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Change participation request status")
    void changeParticipationRequestStatusForUsersEvent_shouldReturn200Status() {
        Long requestId = 4L;
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .status(ParticipationStatus.REJECTED)
                .requestIds(List.of(requestId))
                .build();
        EventRequestStatusUpdateDto statusUpdateDto = EventRequestStatusUpdateDto.builder()
                .confirmedRequests(List.of(participationRequestDto))
                .rejectedRequests(Collections.emptyList())
                .build();

        when(userService.changeParticipationRequestStatusForUsersEvent(userId, eventId, updateRequest))
                .thenReturn(statusUpdateDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests.length()", is(statusUpdateDto.getConfirmedRequests().size())))
                .andExpect(jsonPath("$.rejectedRequests.length()", is(statusUpdateDto.getRejectedRequests().size())));

        verify(userService, times(1)).changeParticipationRequestStatusForUsersEvent(userId, eventId,
                updateRequest);
    }

    @Test
    @SneakyThrows
    @DisplayName("Change participation request status, user not found")
    void changeParticipationRequestStatusForUsersEvent_whenUserNotFound_shouldThrowNotFoundExceptionAnd404Status() {
        Long requestId = 4L;
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .status(ParticipationStatus.REJECTED)
                .requestIds(List.of(requestId))
                .build();

        when(userService.changeParticipationRequestStatusForUsersEvent(userId, eventId, updateRequest))
                .thenThrow(new NotFoundException("User with id '" + userId + "' not found."));

        mvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));

        verify(userService, times(1)).changeParticipationRequestStatusForUsersEvent(userId, eventId,
                updateRequest);
    }

    @Test
    @SneakyThrows
    @DisplayName("Change participation request status, no need to confirm request")
    void changeParticipationRequestStatusForUsersEvent_whenNoNeedToConfirmRequest_shouldThrowEventNotModifiableExceptionAnd409Status() {
        Long requestId = 4L;
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .status(ParticipationStatus.REJECTED)
                .requestIds(List.of(requestId))
                .build();

        when(userService.changeParticipationRequestStatusForUsersEvent(userId, eventId, updateRequest))
                .thenThrow(new EventNotModifiableException("Event with id '" + eventId + "'has no participant limit or " +
                        "pre moderation if off. No need to confirm requests"));

        mvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotModifiableException))
                .andExpect(jsonPath("$.message", is("Event with id '" + eventId + "'has no participant limit or " +
                        "pre moderation if off. No need to confirm requests")))
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated.")));

        verify(userService, times(1)).changeParticipationRequestStatusForUsersEvent(userId, eventId,
                updateRequest);
    }
}