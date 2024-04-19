package ru.practicum.yandex.category.service;

import ru.practicum.yandex.category.model.Category;

public interface CategoryService {
    Category addCategory(Category category);

    Category updateCategory(Long catId, Category updateCategory);

    void removeCategory(Long catId);
}
