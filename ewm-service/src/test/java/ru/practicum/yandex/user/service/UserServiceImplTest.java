package ru.practicum.yandex.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.model.User;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    private User user1;

    private User user2;

    private User user3;

    @BeforeEach
    void init() {
        user1 = createUser(1);
        user2 = createUser(2);
        user3 = createUser(3);
    }


    private User createUser(int id) {
        return User.builder()
                .name("name" + id)
                .email("user" + id + "@email.ru")
                .build();
    }

    @Test
    @DisplayName("Create user")
    void createUser_ShouldReturnUserWithNotNullId() {
        User savedUser = userService.createUser(user1);

        assertThat(savedUser, notNullValue());
        assertThat(savedUser.getId(), notNullValue());
        assertThat(savedUser.getId(), greaterThan(0L));
        assertThat(savedUser.getName(), is(user1.getName()));
        assertThat(savedUser.getEmail(), is(user1.getEmail()));
    }

    @Test
    @DisplayName("Get users from 1 size 2")
    void getUsers_when3UsersExistsFrom1Size2_ShouldReturn2Users() {
        User savedUser1 = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        User savedUser3 = userService.createUser(user3);

        List<User> users = userService.getUsers(null, 1L, 2);

        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.get(0).getId(), is(savedUser2.getId()));
        assertThat(users.get(1).getId(), is(savedUser3.getId()));
    }

    @Test
    @DisplayName("Get users from 0 size 2")
    void getUsers_when3UsersExistsFrom0Size2_ShouldReturn2Users() {
        User savedUser1 = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        User savedUser3 = userService.createUser(user3);

        List<User> users = userService.getUsers(null, 0L, 2);

        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.get(0).getId(), is(savedUser1.getId()));
        assertThat(users.get(1).getId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Get users with ids 1 and 2")
    void getUsers_when3UsersIdsIn1And2_ShouldReturn2Users() {
        User savedUser1 = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        User savedUser3 = userService.createUser(user3);

        List<User> users = userService.getUsers(List.of(savedUser1.getId(), savedUser2.getId()), 0L, 2);

        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.get(0).getId(), is(savedUser1.getId()));
        assertThat(users.get(1).getId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Get users with ids 1, 2 and 999")
    void getUsers_when3UsersIdsIn1And2And999_ShouldReturn2Users() {
        User savedUser1 = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        User savedUser3 = userService.createUser(user3);

        List<User> users = userService.getUsers(List.of(savedUser1.getId(), savedUser2.getId(), 999L), 0L, 2);

        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.get(0).getId(), is(savedUser1.getId()));
        assertThat(users.get(1).getId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Get users with unknown ids")
    void getUsers_whenUnknownIds_ShouldReturnEmptyList() {
        List<User> users = userService.getUsers(List.of(999L), 0L, 2);

        assertThat(users, notNullValue());
        assertThat(users, emptyIterable());
    }

    @Test
    @DisplayName("Delete user")
    void deleteUser_when1UserExists_shouldBeEmptyDb() {
        User savedUser = userService.createUser(user1);

        userService.deleteUser(savedUser.getId());

        List<User> users = userService.getUsers(null, 0L, 10);

        assertThat(users, notNullValue());
        assertThat(users, emptyIterable());
    }

    @Test
    @DisplayName("Delete user with unknown id")
    void deleteUser_whenUnknownId_shouldThrowNotFoundException() {
        Long unknownId = 999L;

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(unknownId));
        assertThat(e.getMessage(), is("User with id '" + unknownId + "' not found."));
    }
}