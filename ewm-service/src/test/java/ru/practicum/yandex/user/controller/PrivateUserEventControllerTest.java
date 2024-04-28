package ru.practicum.yandex.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.yandex.user.dto.EventFullDto;
import ru.practicum.yandex.user.dto.EventShortDto;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.mapper.EventMapper;
import ru.practicum.yandex.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = UserController.class)
class PrivateUserEventControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private EventMapper eventMapper;

    private NewEventDto newEventDto;

    private EventFullDto eventFullDto;

    private EventShortDto eventShortDto;

    @BeforeEach
    void init() {
        newEventDto = new NewEventDto();
        eventFullDto = new EventFullDto();
        eventShortDto = new EventShortDto();
    }

    @Test
    void addEvent() {
    }

    @Test
    void findEventsFromUser() {
    }

    @Test
    void getFullEventByInitiator() {
    }

    @Test
    void updateEvent() {
    }
}