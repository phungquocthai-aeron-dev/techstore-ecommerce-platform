package com.techstore.product.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.product.dto.request.CategoryCreateRequestDTO;
import com.techstore.product.dto.request.CategorySearchRequestDTO;
import com.techstore.product.dto.request.CategoryUpdateRequestDTO;
import com.techstore.product.dto.response.ApiResponse;
import com.techstore.product.dto.response.CategoryResponseDTO;
import com.techstore.product.dto.response.PageResponseDTO;
import com.techstore.product.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponseDTO> createCategory(@RequestBody CategoryCreateRequestDTO requestDTO) {

        return ApiResponse.<CategoryResponseDTO>builder()
                .result(categoryService.createCategory(requestDTO))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponseDTO> updateCategory(
            @PathVariable Long id, @RequestBody CategoryUpdateRequestDTO requestDTO) {

        return ApiResponse.<CategoryResponseDTO>builder()
                .result(categoryService.updateCategory(id, requestDTO))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {

        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {

        return ApiResponse.<CategoryResponseDTO>builder()
                .result(categoryService.getCategoryById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponseDTO<CategoryResponseDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ApiResponse.<PageResponseDTO<CategoryResponseDTO>>builder()
                .result(categoryService.getAllCategories(page, size, sortBy, sortDirection))
                .build();
    }

    @GetMapping("/type/{categoryType}")
    public ApiResponse<PageResponseDTO<CategoryResponseDTO>> getCategoriesByType(
            @PathVariable String categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ApiResponse.<PageResponseDTO<CategoryResponseDTO>>builder()
                .result(categoryService.getCategoriesByType(categoryType, page, size, sortBy, sortDirection))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponseDTO<CategoryResponseDTO>> searchCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        CategorySearchRequestDTO searchDTO = new CategorySearchRequestDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setCategoryType(categoryType);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);

        return ApiResponse.<PageResponseDTO<CategoryResponseDTO>>builder()
                .result(categoryService.searchCategories(searchDTO))
                .build();
    }
}
