package ru.practicum.yandex.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    Category toModel(CategoryDto categoryDto);
}
