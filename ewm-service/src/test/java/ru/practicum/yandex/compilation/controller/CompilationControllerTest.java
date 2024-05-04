package ru.practicum.yandex.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.yandex.compilation.dto.CompilationDto;
import ru.practicum.yandex.compilation.mapper.CompilationMapper;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.compilation.service.CompilationService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompilationController.class)
class CompilationControllerTest {

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
    @DisplayName("Find compilations")
    void findCompilations_shouldReturn200() {
        Boolean pinned = true;
        Long from = 0L;
        Integer size = 24;
        Compilation compilation = new Compilation();
        when(compilationService.findCompilations(pinned, from, size))
                .thenReturn(List.of(compilation));
        when(compilationMapper.toDtoList(List.of(compilation)))
                .thenReturn(List.of(compilationDto));

        mvc.perform(get("/compilations")
                        .param("pinned", String.valueOf(pinned))
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].pinned", is(compilationDto.isPinned())))
                .andExpect(jsonPath("$.[0].title", is(compilationDto.getTitle())));

        verify(compilationService, times(1)).findCompilations(pinned, from, size);
        verify(compilationMapper, times(1)).toDtoList(List.of(compilation));
    }

    @Test
    @SneakyThrows
    @DisplayName("Find compilations")
    void findCompilationById_shouldReturn200() {

        Compilation compilation = new Compilation();
        when(compilationService.findCompilationById(compId))
                .thenReturn(compilation);
        when(compilationMapper.toDto(compilation))
                .thenReturn(compilationDto);

        mvc.perform(get("/compilations/{compId}", compId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned", is(compilationDto.isPinned())))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())));

        verify(compilationService, times(1)).findCompilationById(compId);
        verify(compilationMapper, times(1)).toDto(compilation);
    }

}