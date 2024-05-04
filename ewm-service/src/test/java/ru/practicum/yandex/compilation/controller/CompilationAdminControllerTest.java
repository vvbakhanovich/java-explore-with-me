package ru.practicum.yandex.compilation.controller;

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
import ru.practicum.yandex.compilation.dto.CompilationDto;
import ru.practicum.yandex.compilation.dto.NewCompilationDto;
import ru.practicum.yandex.compilation.dto.UpdateCompilationRequest;
import ru.practicum.yandex.compilation.mapper.CompilationMapper;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.compilation.service.CompilationService;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompilationAdminController.class)
class CompilationAdminControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CompilationService compilationService;

    @MockBean
    private CompilationMapper compilationMapper;

    private CompilationDto compilationDto = CompilationDto.builder()
            .id(1L)
            .title("compilation dto")
            .pinned(true)
            .build();

    private Long compId = 1L;

    @Test
    @SneakyThrows
    @DisplayName("Add compilation")
    void addCompilation_whenTitleValid_shouldReturn201() {
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .title("title")
                .build();
        Compilation compilation = new Compilation();
        when(compilationService.addCompilation(newCompilationDto))
                .thenReturn(compilation);
        when(compilationMapper.toDto(compilation))
                .thenReturn(compilationDto);

        mvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.isPinned())));

        verify(compilationService, times(1)).addCompilation(newCompilationDto);
        verify(compilationMapper, times(1)).toDto(compilation);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add compilation with blank title")
    void addCompilation_whenTitleIsBlank_shouldReturn400() {
        String title = "";
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .title(title)
                .build();

        mvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title can not be blank and must contain between 1 and 50 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(compilationService, never()).addCompilation(any());
        verify(compilationMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add compilation with too long title")
    void addCompilation_whenTitleIsTooLong_shouldReturn400() {
        String title = "a".repeat(51);
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .title(title)
                .build();

        mvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title can not be blank and must contain between 1 and 50 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));
        verify(compilationService, never()).addCompilation(any());
        verify(compilationMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add compilation with title is null")
    void addCompilation_whenTitleIsNull_shouldReturn400() {
        String title = null;
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .title(title)
                .build();

        mvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title can not be blank and must contain between 1 and 50 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));
        verify(compilationService, never()).addCompilation(any());
        verify(compilationMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update with valid title")
    void updateCompilation_whenTitleValid_shouldReturn200() {
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("title")
                .build();

        Compilation compilation = new Compilation();
        when(compilationService.updateCompilation(compId, request))
                .thenReturn(compilation);
        when(compilationMapper.toDto(compilation))
                .thenReturn(compilationDto);

        mvc.perform(patch("/admin/compilations/{compId}", compId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.isPinned())));

        verify(compilationService, times(1)).updateCompilation(compId, request);
        verify(compilationMapper, times(1)).toDto(compilation);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update with blank title")
    void updateCompilation_whenTitleBlank_shouldReturn400() {
        String title = "";
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title(title)
                .build();

        Compilation compilation = new Compilation();
        when(compilationService.updateCompilation(compId, request))
                .thenReturn(compilation);
        when(compilationMapper.toDto(compilation))
                .thenReturn(compilationDto);

        mvc.perform(patch("/admin/compilations/{compId}", compId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title can not be blank and must contain between 1 and 50 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(compilationService, never()).updateCompilation(compId, request);
        verify(compilationMapper, never()).toDto(compilation);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update with too long title")
    void updateCompilation_whenTitleTooLong_shouldReturn400() {
        String title = "a".repeat(51);
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title(title)
                .build();

        Compilation compilation = new Compilation();
        when(compilationService.updateCompilation(compId, request))
                .thenReturn(compilation);
        when(compilationMapper.toDto(compilation))
                .thenReturn(compilationDto);

        mvc.perform(patch("/admin/compilations/{compId}", compId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: title. " +
                        "Error = Title can not be blank and must contain between 1 and 50 characters. " +
                        "Value: " + title)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(compilationService, never()).updateCompilation(compId, request);
        verify(compilationMapper, never()).toDto(compilation);
    }

    @Test
    @SneakyThrows
    @DisplayName("Delete compilation")
    void deleteCompilation_shouldReturn204() {
        mvc.perform(delete("/admin/compilations/{compId}", compId))
                .andExpect(status().isNoContent());
    }
}