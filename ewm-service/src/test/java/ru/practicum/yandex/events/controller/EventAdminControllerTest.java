package ru.practicum.yandex.events.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.service.EventService;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventAdminController.class)
class EventAdminControllerTest {

    @MockBean
    private EventService eventService;

    @MockBean
    private EventMapper eventMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long eventId = 1L;

    @Test
    @SneakyThrows
    @DisplayName("Update event, empty request")
    void updateEventByAdmin_whenAllNullFields_shouldReturn200Status() {
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .build();
        Event event = new Event();
        when(eventService.updateEventByAdmin(eventId, updateRequest))
                .thenReturn(event);

        mvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(eventService, times(1)).updateEventByAdmin(eventId, updateRequest);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event, too short annotation")
    void updateEventByAdmin_whenAllTooShortAnnotation_shouldReturn400Status() {
        String annotation = "a";
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .annotation(annotation)
                .build();

        mvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: annotation. " +
                        "Error = Annotation must not be blank or empty and contain between 20 and 2000 characters. " +
                        "Value: " + annotation)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(eventService, never()).updateEventByAdmin(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event, too long annotation")
    void updateEventByAdmin_whenAllTooLongAnnotation_shouldReturn400Status() {
        String annotation = "a".repeat(2001);
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .annotation(annotation)
                .build();

        mvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: annotation. " +
                        "Error = Annotation must not be blank or empty and contain between 20 and 2000 characters. " +
                        "Value: " + annotation)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(eventService, never()).updateEventByAdmin(any(), any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update event, too negative category id")
    void updateEventByAdmin_whenNegativeCateoryId_shouldReturn400Status() {
        Long categoryId = -1L;
        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .categoryId(categoryId)
                .build();

        mvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: categoryId. " +
                        "Error = Category id must be positive. " +
                        "Value: " + categoryId)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));


        verify(eventService, never()).updateEventByAdmin(any(), any());
        verify(eventMapper, never()).toDto(any());
    }
}