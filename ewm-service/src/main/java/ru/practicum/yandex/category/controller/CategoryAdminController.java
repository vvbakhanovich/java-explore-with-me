package ru.practicum.yandex.category.controller;

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

/**
 * Admin API for categories
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {

    private final CategoryService categoryService;

    private final CategoryMapper categoryMapper;

    /**
     * Add new category. Category name must be unique. If added successfully returns 201 response status.
     *
     * @param categoryDto new category parameters
     * @return added category
     */
    @PostMapping
    @ResponseStatus(CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CategoryDto categoryDto) {
        log.info("Adding category '{}'.", categoryDto);
        final Category category = categoryMapper.toModel(categoryDto);
        final Category addedCategory = categoryService.addCategory(category);
        return categoryMapper.toDto(addedCategory);
    }

    /**
     * Update category. Category name must be unique. If category not found returns 404 response status.
     *
     * @param catId             category id to update
     * @param updateCategoryDto category parameters to update
     * @return updated category
     */
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid CategoryDto updateCategoryDto) {
        log.info("Updating category with id '{}', new name: '{}'.", catId, updateCategoryDto.getName());
        final Category updateCategory = categoryMapper.toModel(updateCategoryDto);
        final Category updatedCategory = categoryService.updateCategory(catId, updateCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    /**
     * Delete category by category id. Category can not be linked to any events, otherwise returns 409 response status.
     * If category deleted successfully, returns 204 response status.
     *
     * @param catId category id to delete
     */
    @DeleteMapping("/{catId}")
    @ResponseStatus(NO_CONTENT)
    public void removeCategory(@PathVariable Long catId) {
        log.info("Deleting category with id '{}'.", catId);
        categoryService.removeCategoryById(catId);
    }
}
