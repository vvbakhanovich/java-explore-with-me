package ru.practicum.yandex.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.dto.NewUserRequest;
import ru.practicum.yandex.user.dto.UserDto;
import ru.practicum.yandex.user.mapper.UserMapper;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserAdminController.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    private User user;

    private UserDto userDto;

    private NewUserRequest userShortDto;

    @BeforeEach
    void init() {
        userDto = UserDto.builder()
                .email("testDto@email.com")
                .name("nameDto")
                .build();
        user = User.builder()
                .email("test@email.com")
                .name("name")
                .build();
        userShortDto = NewUserRequest.builder()
                .email("testShort@email.com")
                .name("nameShort")
                .build();
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user")
    void createUser_whenAllFieldAreValid_shouldReturnStatus201() {
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userDto.getName())));

        verify(userMapper, times(1)).toModel(userShortDto);
        verify(userService, times(1)).createUser(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with empty name")
    void createUser_whenEmptyName_shouldThrowMethodArgumentNotValidException() {
        userShortDto.setName("");
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: name. " +
                        "Error = Name length must be between 2 and 250 characters. Value: ")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with name consisting of whitespaces")
    void createUser_whenNameConsistsOfWhiteSpaces_shouldThrowMethodArgumentNotValidException() {
        userShortDto.setName("   ");
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message",
                        is("Field: name. Error = Name length must be between 2 and 250 characters. Value:    ")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with too long name")
    void createUser_whenNameIsTooLong_shouldThrowMethodArgumentNotValidException() {
        String name = "a".repeat(251);
        userShortDto.setName(name);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message",
                        is("Field: name. Error = Name length must be between 2 and 250 characters. Value: " + name)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user without name")
    void createUser_whenNameIsNull_shouldThrowMethodArgumentNotValidException() {
        userShortDto.setName(null);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message",
                        is("Field: name. Error = Name length must be between 2 and 250 characters. Value: null")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with empty email")
    void createUser_whenEmptyEmail_shouldThrowMethodArgumentNotValidException() {
        userShortDto.setEmail("");
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: email. Error = Wrong email format. Value: ")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with email consisting of whitespaces")
    void createUser_whenEmailConsistsOfWhitespaces_shouldThrowMethodArgumentNotValidException() {
        userShortDto.setEmail("    ");
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: email. Error = Wrong email format. Value:     ")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with too long domain part email")
    void createUser_whenTooLongDomainPartOfEmail_shouldThrowMethodArgumentNotValidException() {
        String email = "a@" + "b".repeat(64) + ".ru";
        userShortDto.setEmail(email);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: email. Error = Wrong email format. Value: " + email)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with too long email")
    void createUser_whenTooLongEmail_shouldThrowMethodArgumentNotValidException() {
        String email = "a@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(63) + "." +
                "e".repeat(61);
        userShortDto.setEmail(email);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: email. Error = Wrong email format. Value: " + email)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with max length email")
    void createUser_whenEmailMaxLength_shouldReturn201() {
        String email = "a@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(63) + "." +
                "e".repeat(60);
        userShortDto.setEmail(email);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userDto.getName())));

        verify(userMapper, times(1)).toModel(userShortDto);
        verify(userService, times(1)).createUser(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with too long local part email")
    void createUser_whenTooLongLocalPartOfEmail_shouldThrowMethodArgumentNotValidException() {
        String email = "a".repeat(65) + "@b.ru";
        userShortDto.setEmail(email);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: email. Error = Wrong email format. Value: " + email)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with max length local part email")
    void createUser_whenMaxLengthOfLocalPartOfEmail_shouldThrowMethodArgumentNotValidException() {
        String email = "a".repeat(58) + "@b.ru";
        userShortDto.setEmail(email);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userDto.getName())));

        verify(userMapper, times(1)).toModel(userShortDto);
        verify(userService, times(1)).createUser(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with too short email")
    void createUser_whenTooShortEmail_shouldThrowMethodArgumentNotValidException() {
        String email = "a@b.r";
        userShortDto.setEmail(email);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: email. Error = Wrong email format. Value: a@b.r")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with min allowed email length")
    void createUser_whenEmailLengthIsAllowed_shouldThrowMethodArgumentNotValidException() {
        String email = "b@a.ru";
        userShortDto.setEmail(email);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userDto.getName())));

        verify(userMapper, times(1)).toModel(userShortDto);
        verify(userService, times(1)).createUser(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Create user with without email")
    void createUser_whenEmailIsNull_shouldThrowMethodArgumentNotValidException() {
        userShortDto.setEmail(null);
        when(userMapper.toModel(userShortDto))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message",
                        is("Field: email. Error = Wrong email format. Value: null")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(userMapper, never()).toModel(any(NewUserRequest.class));
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Get users with default size")
    void getUsers_WithoutSizeParam_ShouldReturnListWith10Elements() {
        Long from = 12L;
        when(userService.getUsers(null, from, 10))
                .thenReturn(List.of(user));
        when(userMapper.toDtoList(List.of(user)))
                .thenReturn(List.of(userDto));

        mvc.perform(get("/admin/users")
                        .param("from", String.valueOf(from)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(userDto.getId())))
                .andExpect(jsonPath("$.[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail())));

        verify(userService, times(1)).getUsers(null, from, 10);
        verify(userMapper, times(1)).toDtoList(List.of(user));
    }

    @Test
    @SneakyThrows
    @DisplayName("Get users with default size")
    void getUsers_WithoutFromParam_ShouldReturnListWith10Elements() {
        Integer size = 13;
        when(userService.getUsers(null, 0L, size))
                .thenReturn(List.of(user));
        when(userMapper.toDtoList(List.of(user)))
                .thenReturn(List.of(userDto));

        mvc.perform(get("/admin/users")
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(userDto.getId())))
                .andExpect(jsonPath("$.[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail())));

        verify(userService, times(1)).getUsers(null, 0L, size);
        verify(userMapper, times(1)).toDtoList(List.of(user));
    }

    @Test
    @SneakyThrows
    @DisplayName("Attempt to delete non existing user")
    void deleteUser_whenUserNotFound_ShouldThrowNotFoundException() {
        Long userId = 1L;

        doThrow(new NotFoundException("User with id '" + userId + "' not found.")).when(userService).deleteUser(userId);

        mvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User with id '" + userId + "' not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("The required object was not found.")));

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Delete user")
    void deleteUser_whenUserFound_ShouldThrowNotFoundException() {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        mvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }
}