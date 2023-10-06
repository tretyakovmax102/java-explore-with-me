package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;


@RequiredArgsConstructor
@Service
public class AdminCategoryService {
    private final CategoryService categoryService;

    public CategoryDto postCategory(NewCategoryDto newCategoryDto) {
        return categoryService.postCategory(newCategoryDto);
    }

    public CategoryDto patchCategory(NewCategoryDto newCategoryDto, Integer id) {
        return categoryService.updateCategory(newCategoryDto, id);
    }

    public void deleteCategory(Integer id) {
        categoryService.deleteCategory(id);
    }
}
