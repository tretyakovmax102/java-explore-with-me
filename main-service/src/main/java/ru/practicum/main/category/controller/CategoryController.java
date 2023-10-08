package ru.practicum.main.category.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.service.CategoryService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
public class CategoryController {
    private final CategoryService categoriesService;

    @GetMapping("/categories")
    public List<CategoryDto> findAll(@PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
                                     @Positive @RequestParam(required = false, defaultValue = "10") int size) {
        return categoriesService.getCategories(from, size);
    }

    @GetMapping("categories/{catId}")
    public CategoryDto find(@PathVariable int catId) {
        return categoriesService.getCategory(catId);
    }

    @PostMapping(value = "admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto add(@Valid @RequestBody NewCategoryDto categoryDto) {
        return categoriesService.postCategory(categoryDto);
    }

    @DeleteMapping(value = "admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int catId) {
        categoriesService.deleteCategory(catId);
    }

    @PatchMapping(value = "admin/categories/{catId}")
    public CategoryDto update(@PathVariable int catId,
                              @Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoriesService.updateCategory(newCategoryDto, catId);
    }
}
