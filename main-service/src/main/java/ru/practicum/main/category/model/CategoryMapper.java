package ru.practicum.main.category.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {

    public static CategoryDto categoryToDto(Category category) {
        return CategoryDto.builder().name(category.getName()).id(category.getId()).build();
    }

    public static Category categoryFromInputDto(NewCategoryDto newCategoryDto) {
        return Category.builder().name(newCategoryDto.getName()).build();
    }
}