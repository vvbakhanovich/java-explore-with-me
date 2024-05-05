package ru.practicum.yandex.category.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Captor
    private ArgumentCaptor<Category> categoryArgumentCaptor;

    private Category category;

    private Category updateCategory;

    private Long catId;

    @BeforeEach
    void init() {
        category = Category.builder()
                .name("name")
                .build();
        updateCategory = Category.builder()
                .name("update name")
                .build();
        catId = 1L;
    }

    @Test
    void updateCategory_shouldUpdateCategoryName() {
        when(categoryRepository.findById(catId))
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(any()))
                .thenReturn(category);

        categoryService.updateCategory(catId, updateCategory);

        verify(categoryRepository, times(1)).save(categoryArgumentCaptor.capture());
        Category updatedCategory = categoryArgumentCaptor.getValue();

        assertThat(updatedCategory.getName(), is(updateCategory.getName()));
    }
}