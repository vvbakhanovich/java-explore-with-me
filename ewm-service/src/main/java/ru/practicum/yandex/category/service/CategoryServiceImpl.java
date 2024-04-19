package ru.practicum.yandex.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.shared.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category addCategory(Category category) {
        final Category savedCategory = categoryRepository.save(category);
        log.info("CategoryController, category with id '{}' was saved.", savedCategory.getId());
        return savedCategory;
    }

    @Override
    public Category updateCategory(Long catId, Category updateCategory) {
        final Category foundCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id '" + catId + "' not found."));
        foundCategory.setName(updateCategory.getName());
        final Category updatedCategory = categoryRepository.save(foundCategory);
        log.info("CategoryController, update category with id '{}', new name: '{}'.", catId, updatedCategory.getName());
        return updatedCategory;
    }

    @Override
    public void removeCategory(Long catId) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id '" + catId + "' not found."));
        categoryRepository.deleteById(catId);
        log.info("CategoryController, deleted category with id '" + catId + "'.");
    }
}
