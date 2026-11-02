package com.kei.backend.category;

import com.kei.backend.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getAll(@CurrentUserId Long userId,
            @RequestParam(required = false) CategoryType type) {
        if (type != null) {
            return categoryService.getByType(userId, type);
        } else {
            return categoryService.getAll(userId);
        }
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(@CurrentUserId Long userId, @PathVariable Long id) {
        return categoryService.getById(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@CurrentUserId Long userId, @Valid @RequestBody CategoryRequest request) {
        return categoryService.create(userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @PathVariable Long id) {
        categoryService.delete(userId, id);
    }
}
