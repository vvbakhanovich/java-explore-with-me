package ru.practicum.yandex.category.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.shared.exception.NotFoundException;

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
class CategoryServiceImplIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    private Category category1;
    private Category category2;
    private Category category3;

    @BeforeEach
    void init() {
        category1 = createCategory(1);
        category2 = createCategory(2);
        category3 = createCategory(3);
    }

    @Test
    @DisplayName("Add category")
    void addCategory_shouldReturnCategoryWithNotNullId() {
        Category addedCategory = categoryService.addCategory(category1);

        assertThat(addedCategory, notNullValue());
        assertThat(addedCategory.getId(), greaterThan(0L));
    }

    @Test
    @DisplayName("Update category")
    void updateCategory_whenCategoryExists_shouldUpdateCategoryName() {
        Category addedCategory = categoryService.addCategory(category1);
        Long categoryId = addedCategory.getId();

        Category updatedCategory = categoryService.updateCategory(categoryId, category2);

        assertThat(updatedCategory, notNullValue());
        assertThat(updatedCategory.getId(), is(categoryId));
        assertThat(updatedCategory.getName(), is(category2.getName()));
    }

    @Test
    @DisplayName("Update non existing category")
    void updateCategory_whenNotFound_shouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> categoryService.updateCategory(999L, category1));

        assertThat(e.getMessage(), is("Category with id '999' not found."));
    }

    @Test
    @DisplayName("Find all categories")
    void findCategories_whenFromIs0SizeIs10_ShouldReturnAllCategories() {
        Category addedCategory1 = categoryService.addCategory(category1);
        Category addedCategory2 = categoryService.addCategory(category2);
        Category addedCategory3 = categoryService.addCategory(category3);

        List<Category> categories = categoryService.findCategories(0L, 10);

        assertThat(categories, notNullValue());
        assertThat(categories.size(), is(3));
        assertThat(categories.get(0).getId(), is(addedCategory1.getId()));
        assertThat(categories.get(1).getId(), is(addedCategory2.getId()));
        assertThat(categories.get(2).getId(), is(addedCategory3.getId()));
    }

    @Test
    @DisplayName("Find categories from 2nd element")
    void findCategories_whenFromIs1SizeIs10_ShouldReturnAllButFirstCategories() {
        Category addedCategory1 = categoryService.addCategory(category1);
        Category addedCategory2 = categoryService.addCategory(category2);
        Category addedCategory3 = categoryService.addCategory(category3);

        List<Category> categories = categoryService.findCategories(1L, 10);

        assertThat(categories, notNullValue());
        assertThat(categories.size(), is(2));
        assertThat(categories.get(0).getId(), is(addedCategory2.getId()));
        assertThat(categories.get(1).getId(), is(addedCategory3.getId()));
    }

    @Test
    @DisplayName("Find first 2 categories")
    void findCategories_whenFromIs0SizeIs2_ShouldReturnFirstTwoCategories() {
        Category addedCategory1 = categoryService.addCategory(category1);
        Category addedCategory2 = categoryService.addCategory(category2);
        Category addedCategory3 = categoryService.addCategory(category3);

        List<Category> categories = categoryService.findCategories(0L, 2);

        assertThat(categories, notNullValue());
        assertThat(categories.size(), is(2));
        assertThat(categories.get(0).getId(), is(addedCategory1.getId()));
        assertThat(categories.get(1).getId(), is(addedCategory2.getId()));
    }

    @Test
    @DisplayName("Find empty list")
    void findCategories_whenNotCategories_ShouldReturnEmptyList() {

        List<Category> categories = categoryService.findCategories(0L, 2);

        assertThat(categories, notNullValue());
        assertThat(categories, is(emptyIterable()));
    }

    @Test
    @DisplayName("Find category by id")
    void findCategoryById_whenCategoryExists_shouldReturnCategory() {
        Category addedCategory = categoryService.addCategory(category1);
        Long categoryId = addedCategory.getId();

        Category foundCategory = categoryService.findCategoryById(categoryId);

        assertThat(foundCategory, notNullValue());
        assertThat(foundCategory.getId(), is(addedCategory.getId()));
        assertThat(foundCategory.getName(), is(addedCategory.getName()));
    }

    @Test
    void findCategoryById_whenCategoryNotFound_shouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> categoryService.findCategoryById(999L));

        assertThat(e.getMessage(), is("Category with id '999' not found."));
    }

    @Test
    @DisplayName("Delete category")
    void removeCategoryById_whenCategoryExists_shouldDeleteCategoryFromDb() {
        Category addedCategory = categoryService.addCategory(category1);
        Long categoryId = addedCategory.getId();

        categoryService.removeCategoryById(categoryId);
        List<Category> categories = categoryService.findCategories(0L, 10);

        assertThat(categories, notNullValue());
        assertThat(categories, emptyIterable());
    }

    @Test
    @DisplayName("Delete non existing category")
    void removeCategoryById_whenCategoryNotFound_shouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> categoryService.removeCategoryById(999L));

        assertThat(e.getMessage(), is("Category with id '999' not found."));
    }

    private Category createCategory(int id) {
        return Category.builder()
                .name("category " + id)
                .build();
    }
}