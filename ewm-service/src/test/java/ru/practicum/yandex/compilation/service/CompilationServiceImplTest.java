package ru.practicum.yandex.compilation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.compilation.dto.NewCompilationDto;
import ru.practicum.yandex.compilation.dto.UpdateCompilationRequest;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.Location;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class CompilationServiceImplTest {

    @Autowired
    private CompilationService compilationService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Event savedEvent1;

    private Event savedEvent2;

    private Category savedCategory;

    private Location location;

    @BeforeEach
    void init() {
        User user = User.builder()
                .name("name")
                .email("email@email.com")
                .build();
        User savedUser = userService.createUser(user);
        Category category = Category.builder()
                .name("category name")
                .build();
        location = Location.builder()
                .lat(24F)
                .lon(54.2F)
                .build();
        savedCategory = categoryRepository.save(category);
        savedEvent1 = userService.addEventByUser(savedUser.getId(), createNewEvent(1));
        savedEvent2 = userService.addEventByUser(savedUser.getId(), createNewEvent(2));
    }

    @Test
    @DisplayName("Add one event to compilation")
    void addCompilation_whenCompilationOfOneEvent_shouldReturnCompilation() {
        NewCompilationDto compilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("compilation of one event title")
                .events(List.of(savedEvent1.getId()))
                .build();

        Compilation compilation = compilationService.addCompilation(compilationDto);

        assertThat(compilation, notNullValue());
        assertThat(compilation.getTitle(), is(compilationDto.getTitle()));
        assertThat(compilation.isPinned(), is(compilationDto.isPinned()));
        assertThat(compilation.getEvents().size(), is(1));
        assertThat(compilation.getEvents().get(0).getId(), is(savedEvent1.getId()));
    }

    @Test
    @DisplayName("Add compilation without events")
    void addCompilation_whenCompilationWithoutEvent() {
        NewCompilationDto compilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("compilation of one event title")
                .build();

        Compilation compilation = compilationService.addCompilation(compilationDto);

        assertThat(compilation, notNullValue());
        assertThat(compilation.getTitle(), is(compilationDto.getTitle()));
        assertThat(compilation.isPinned(), is(compilationDto.isPinned()));
        assertThat(compilation.getEvents().size(), is(0));
    }

    @Test
    @DisplayName("Add compilation of two events")
    void addCompilation_whenCompilationOfTwoEvents() {
        NewCompilationDto compilationDto = NewCompilationDto.builder()
                .pinned(false)
                .events(List.of(savedEvent1.getId(), savedEvent2.getId()))
                .title("compilation of one event title")
                .build();

        Compilation compilation = compilationService.addCompilation(compilationDto);

        assertThat(compilation, notNullValue());
        assertThat(compilation.getTitle(), is(compilationDto.getTitle()));
        assertThat(compilation.isPinned(), is(compilationDto.isPinned()));
        assertThat(compilation.getEvents().size(), is(2));
        assertThat(compilation.getEvents().get(0).getId(), is(savedEvent1.getId()));
        assertThat(compilation.getEvents().get(1).getId(), is(savedEvent2.getId()));
    }


    @Test
    @DisplayName("Update events for compilation")
    void updateCompilation_whenNewEvent_shouldContainNewEventList() {
        NewCompilationDto compilationDto = NewCompilationDto.builder()
                .pinned(false)
                .events(List.of(savedEvent1.getId(), savedEvent2.getId()))
                .title("compilation of one event title")
                .build();
        Compilation compilation = compilationService.addCompilation(compilationDto);

        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .events(List.of(savedEvent1.getId()))
                .build();
        Compilation updatedCompilation = compilationService.updateCompilation(compilation.getId(), updateRequest);

        assertThat(updatedCompilation, notNullValue());
        assertThat(updatedCompilation.getId(), is(compilation.getId()));
        assertThat(updatedCompilation.getEvents().size(), is(1));
        assertThat(updatedCompilation.getEvents().get(0).getId(), is(savedEvent1.getId()));
        assertThat(updatedCompilation.getTitle(), is(compilation.getTitle()));
        assertThat(updatedCompilation.isPinned(), is(compilation.isPinned()));
    }

    @Test
    @DisplayName("Update events for compilation, add to empty compilation")
    void updateCompilation_whenEmptyCompilation_shouldContainNewEventList() {
        NewCompilationDto compilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("compilation of one event title")
                .build();
        Compilation compilation = compilationService.addCompilation(compilationDto);

        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .events(List.of(savedEvent1.getId()))
                .build();
        Compilation updatedCompilation = compilationService.updateCompilation(compilation.getId(), updateRequest);

        assertThat(updatedCompilation, notNullValue());
        assertThat(updatedCompilation.getId(), is(compilation.getId()));
        assertThat(updatedCompilation.getEvents().size(), is(1));
        assertThat(updatedCompilation.getEvents().get(0).getId(), is(savedEvent1.getId()));
        assertThat(updatedCompilation.getTitle(), is(compilation.getTitle()));
        assertThat(updatedCompilation.isPinned(), is(compilation.isPinned()));
    }

    @Test
    @DisplayName("Delete compilation")
    void deleteCompilation_whenCompilationExists_shouldDeleteCompilation() {
        NewCompilationDto compilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("compilation of one event title")
                .build();
        Compilation compilation = compilationService.addCompilation(compilationDto);

        compilationService.deleteCompilation(compilation.getId());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> compilationService.findCompilationById(compilation.getId()));
        assertThat(e.getMessage(), is("Compilation with id '" + compilation.getId() + "' not found."));
    }

    @Test
    @DisplayName("Delete unknown compilation")
    void deleteCompilation_whenCompilationNotExists_shouldThrowNotFoundException() {
        long compId = 999L;

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> compilationService.deleteCompilation(compId));
        assertThat(e.getMessage(), is("Compilation with id '" + compId + "' not found."));
    }

    @Test
    @DisplayName("Find pinned compilations")
    void findCompilations_whenCompilationsPinned_shouldReturnCompilationList() {
        NewCompilationDto compilationDto1 = NewCompilationDto.builder()
                .pinned(false)
                .title("compilation of empty event title")
                .build();
        Compilation compilation1 = compilationService.addCompilation(compilationDto1);
        NewCompilationDto compilationDto2 = NewCompilationDto.builder()
                .pinned(true)
                .events(List.of(savedEvent1.getId()))
                .title("compilation of one event title")
                .build();
        Compilation compilation2 = compilationService.addCompilation(compilationDto2);

        List<Compilation> compilations = compilationService.findCompilations(true, 0L, 10);

        assertThat(compilations, notNullValue());
        assertThat(compilations.size(), is(1));
        assertThat(compilations.get(0).getId(), is(compilation2.getId()));
    }

    @Test
    @DisplayName("Find all compilations")
    void findCompilations_whenCompilationsPinnedNull_shouldReturnCompilationList() {
        NewCompilationDto compilationDto1 = NewCompilationDto.builder()
                .pinned(false)
                .title("compilation of empty event title")
                .build();
        Compilation compilation1 = compilationService.addCompilation(compilationDto1);
        NewCompilationDto compilationDto2 = NewCompilationDto.builder()
                .pinned(true)
                .events(List.of(savedEvent1.getId()))
                .title("compilation of one event title")
                .build();
        Compilation compilation2 = compilationService.addCompilation(compilationDto2);

        List<Compilation> compilations = compilationService.findCompilations(null, 0L, 10);

        assertThat(compilations, notNullValue());
        assertThat(compilations.size(), is(2));
        assertThat(compilations.get(0).getId(), is(compilation1.getId()));
        assertThat(compilations.get(1).getId(), is(compilation2.getId()));
    }

    @Test
    @DisplayName("Find all compilations from 2nd element")
    void findCompilations_whenCompilationsPinnedNullFrom2Element_shouldReturnCompilationList() {
        NewCompilationDto compilationDto1 = NewCompilationDto.builder()
                .pinned(false)
                .title("compilation of empty event title")
                .build();
        Compilation compilation1 = compilationService.addCompilation(compilationDto1);
        NewCompilationDto compilationDto2 = NewCompilationDto.builder()
                .pinned(true)
                .events(List.of(savedEvent1.getId()))
                .title("compilation of one event title")
                .build();
        Compilation compilation2 = compilationService.addCompilation(compilationDto2);

        List<Compilation> compilations = compilationService.findCompilations(null, 1L, 10);

        assertThat(compilations, notNullValue());
        assertThat(compilations.size(), is(1));
        assertThat(compilations.get(0).getId(), is(compilation2.getId()));
    }

    @Test
    @DisplayName("Find compilation")
    void findCompilationById_whenCompilationExists_shouldReturnCompilation() {
        NewCompilationDto compilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("compilation of one event title")
                .build();
        Compilation compilation = compilationService.addCompilation(compilationDto);

        Compilation foundCompilation = compilationService.findCompilationById(compilation.getId());

        assertThat(foundCompilation.getId(), is(compilation.getId()));
    }

    @Test
    @DisplayName("Find non existing compilation")
    void findCompilationById_whenCompilationNotExists_shouldReturnThrowNofFoundException() {
        long compId = 999L;

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> compilationService.findCompilationById(compId));
        assertThat(e.getMessage(), is("Compilation with id '" + compId + "' not found."));
    }

    private NewEvent createNewEvent(int id) {
        return NewEvent.builder()
                .annotation("event annotation test " + id)
                .eventDate(LocalDateTime.of(2030, 3, 13, 11, 23, 43))
                .categoryId(savedCategory.getId())
                .participantLimit(213)
                .description("event description " + id)
                .paid(false)
                .requestModeration(false)
                .title("event title" + id)
                .location(location)
                .build();
    }
}