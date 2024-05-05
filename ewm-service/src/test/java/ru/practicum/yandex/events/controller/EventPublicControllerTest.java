package ru.practicum.yandex.events.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.yandex.StatClient;
import ru.practicum.yandex.dto.ViewStatsDto;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.service.EventService;
import ru.practicum.yandex.shared.exception.IncorrectDateRangeException;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventPublicController.class)
class EventPublicControllerTest {

    @MockBean
    private EventService eventService;

    @MockBean
    private EventMapper eventMapper;

    @MockBean
    private StatClient statClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long eventId = 1L;

    @Test
    @SneakyThrows
    @DisplayName("Find event not valid dates in filter")
    void findEvents_whenDateRangeIsInvalid_shouldThrowIncorrectDateRangeException() {
        mvc.perform(get("/events")
                        .param("rangeStart", "2030-10-10 10:10:10")
                        .param("rangeEnd", "2020-10-10 10:10:10"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IncorrectDateRangeException))
                .andExpect(jsonPath("$.message", is("Wrong date range.")))
                .andExpect(jsonPath("$.reason", is("Incorrect date range.")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")));

        verify(eventService, never()).findEvents(any(), any(), any());
        verify(eventMapper, never()).toShortDtoList(any());
        verify(statClient, never()).methodHit(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Find event with valid dates in filter")
    void findEvents_whenDateRangeIsValid_shouldThrowIncorrectDateRangeException() {
        mvc.perform(get("/events")
                        .param("rangeStart", "2020-10-10 10:10:10")
                        .param("rangeEnd", "2030-10-10 10:10:10"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).findEvents(any(), eq(0L), eq(10));
        verify(eventMapper, times(1)).toShortDtoList(any());
        verify(statClient, times(1)).methodHit(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Get event info")
    void getFullEventInfoById_shouldReturn200() {
        ViewStatsDto viewStatsDto = new ViewStatsDto("app", "uri",5L);
        when(statClient.getUniqueIpStatsForUri("/events/" + eventId))
                .thenReturn(viewStatsDto);
        mvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk());

        verify(statClient, times(1)).methodHit(any());
        verify(statClient, times(1)).getUniqueIpStatsForUri("/events/" + eventId);
        verify(eventService, times(1)).getFullEventInfoById(eventId, viewStatsDto.getHits());
        verify(eventMapper, times(1)).toDto(any());
    }
}