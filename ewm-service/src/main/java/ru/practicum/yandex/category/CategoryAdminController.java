package ru.practicum.yandex.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.category.mapper.CategoryMapper;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.service.CategoryService;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {

    private final CategoryService categoryService;

    private final CategoryMapper categoryMapper;

    @PostMapping
    @ResponseStatus(CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CategoryDto categoryDto) {
        log.info("CategoryAdminController adding category '{}'.", categoryDto);
        Category category = categoryMapper.toModel(categoryDto);
        Category addedCategory = categoryService.addCategory(category);
        return categoryMapper.toDto(addedCategory);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId) {
        log.info("CategoryAdminController updating category with id '{}'.", catId);
        Category updatedCategory = categoryService.updateCategory(catId);
        return categoryMapper.toDto(updatedCategory);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(NO_CONTENT)
    public void removeCategory(@PathVariable Long catId) {
        log.info("CategoryAdminController deleting category with id '{}'.", catId);
        categoryService.removeCategory(catId);
    }
}
