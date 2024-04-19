package ru.practicum.yandex.category;

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
import ru.practicum.yandex.category.controller.CategoryAdminController;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.category.mapper.CategoryMapper;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.service.CategoryService;
import ru.practicum.yandex.shared.exception.NotFoundException;

import javax.validation.ConstraintViolationException;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryAdminController.class)
class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryMapper categoryMapper;

    @MockBean
    private CategoryService categoryService;

    private Category category;

    private CategoryDto categoryDto;

    private Long catId;

    @BeforeEach
    void init() {
        category = Category.builder()
                .name("category")
                .build();
        categoryDto = CategoryDto.builder()
                .name("categoryDto")
                .build();
        catId = 1L;
    }

    @Test
    @SneakyThrows
    @DisplayName("Add category")
    void addCategory_whenNameValid_ShouldReturn201Status() {
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.addCategory(category))
                .thenReturn(category);
        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(categoryDto.getId())))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));

        verify(categoryMapper, times(1)).toModel(categoryDto);
        verify(categoryService, times(1)).addCategory(category);
        verify(categoryMapper, times(1)).toDto(category);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add category with min length name")
    void addCategory_whenNameIsMinLength_ShouldReturn201Status() {
        String name = "a";
        categoryDto.setName(name);
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.addCategory(category))
                .thenReturn(category);
        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(categoryDto.getId())))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));

        verify(categoryMapper, times(1)).toModel(categoryDto);
        verify(categoryService, times(1)).addCategory(category);
        verify(categoryMapper, times(1)).toDto(category);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add category with max length name")
    void addCategory_whenNameIsMaxLength_ShouldReturn201Status() {
        String name = "a".repeat(50);
        categoryDto.setName(name);
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.addCategory(category))
                .thenReturn(category);
        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(categoryDto.getId())))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));

        verify(categoryMapper, times(1)).toModel(categoryDto);
        verify(categoryService, times(1)).addCategory(category);
        verify(categoryMapper, times(1)).toDto(category);
    }

    @Test
    @SneakyThrows
    @DisplayName("Add category with too long name")
    void addCategory_whenNameIsTooLong_ShouldReturn400StatusAndThrowConstraintViolationException() {
        String name = "a".repeat(51);
        categoryDto.setName(name);
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.addCategory(category))
                .thenReturn(category);
        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: name. " +
                        "Error = Name must no be blank or empty and must have length between 1 and 50 characters. " +
                        "Value: " + name)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(categoryMapper, never()).toModel(any());
        verify(categoryService, never()).addCategory(any());
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add category with empty name")
    void addCategory_whenNameIsEmpty_ShouldReturn400StatusAndThrowConstraintViolationException() {
        String name = "";
        categoryDto.setName(name);
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.addCategory(category))
                .thenReturn(category);
        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: name. " +
                        "Error = Name must no be blank or empty and must have length between 1 and 50 characters. " +
                        "Value: " + name)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(categoryMapper, never()).toModel(any());
        verify(categoryService, never()).addCategory(any());
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add category with blank name")
    void addCategory_whenNameIsBlank_ShouldReturn400StatusAndThrowConstraintViolationException() {
        String name = "   ";
        categoryDto.setName(name);
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.addCategory(category))
                .thenReturn(category);
        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Field: name. " +
                        "Error = Name must no be blank or empty and must have length between 1 and 50 characters. " +
                        "Value: " + name)))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Incorrectly made request.")));

        verify(categoryMapper, never()).toModel(any());
        verify(categoryService, never()).addCategory(any());
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Add category with blank name")
    void addCategory_whenServiceThrowsConstraintViolationException_ShouldReturn409Status() {
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.addCategory(category))
                .thenThrow(ConstraintViolationException.class);

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated.")));

        verify(categoryMapper, times(1)).toModel(categoryDto);
        verify(categoryService, times(1)).addCategory(category);
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Update category")
    void updateCategory_whenNameIsValid_shouldReturn200Status() {
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.updateCategory(catId, category))
                .thenReturn(category);
        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);

        mvc.perform(patch("/admin/categories/{catId}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryDto.getId())))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));

        verify(categoryMapper, times(1)).toModel(categoryDto);
        verify(categoryService, times(1)).updateCategory(catId, category);
        verify(categoryMapper, times(1)).toDto(category);
    }

    @Test
    @SneakyThrows
    @DisplayName("Update not found category")
    void updateCategory_whenCategoryNotFound_shouldReturn200Status() {
        when(categoryMapper.toModel(categoryDto))
                .thenReturn(category);
        when(categoryService.updateCategory(catId, category))
                .thenThrow(new NotFoundException("Category with id '" + catId + "' not found."));

        mvc.perform(patch("/admin/categories/{catId}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.message", is("Category with id '" + catId + "' not found.")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")));

        verify(categoryMapper, times(1)).toModel(categoryDto);
        verify(categoryService, times(1)).updateCategory(catId, category);
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Delete category")
    void removeCategory_whenCategoryFound_shouldReturn204Status() {
        doNothing().when(categoryService).removeCategory(catId);

        mvc.perform(delete("/admin/categories/{catId}", catId))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).removeCategory(catId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Delete not found category")
    void removeCategory_whenCategoryNotFound_shouldReturn204Status() {
        mvc.perform(delete("/admin/categories/{catId}", catId))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).removeCategory(catId);
    }
}