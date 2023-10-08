package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.model.CategoryMapper;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventService eventService;

    public CategoryDto getCategory(Integer id) {
        return CategoryMapper.categoryToDto(categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("not found category with id = " + id))));
    }

    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from, size))
                .getContent()
                .stream()
                .map(CategoryMapper::categoryToDto)
                .collect(Collectors.toList());
    }

    public CategoryDto postCategory(NewCategoryDto newCategoryDto) {
        return CategoryMapper.categoryToDto(categoryRepository.save(CategoryMapper.categoryFromInputDto(newCategoryDto)));
    }

    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Integer id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format(" not found category with id = " + id)));
        category.setName(newCategoryDto.getName());
        return CategoryMapper.categoryToDto(categoryRepository.save(category));
    }

    public void deleteCategory(Integer id) {
        if (eventService.findFirstByCategoryId(id) == null) {
            categoryRepository.delete(categoryRepository.findById(id).orElseThrow(
                    () -> new NotFoundException(String.format("not found category with id = " + id))));
        } else {
            throw new ConflictException("category include event!");
        }
    }

}
