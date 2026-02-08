package com.techstore.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.techstore.product.dto.response.CategoryResponseDTO;
import com.techstore.product.dto.response.PageResponseDTO;
import com.techstore.product.service.CategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Thêm category mới (ADMIN only)
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryCreateRequestDTO requestDTO) {
        CategoryResponseDTO response = categoryService.createCategory(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Cập nhật category (ADMIN only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id, @RequestBody CategoryUpdateRequestDTO requestDTO) {
        CategoryResponseDTO response = categoryService.updateCategory(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa category (ADMIN only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy category theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tất cả categories (phân trang)
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        PageResponseDTO<CategoryResponseDTO> response =
                categoryService.getAllCategories(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy categories theo categoryType
     */
    @GetMapping("/type/{categoryType}")
    public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> getCategoriesByType(
            @PathVariable String categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        PageResponseDTO<CategoryResponseDTO> response =
                categoryService.getCategoriesByType(categoryType, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Tìm kiếm categories
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> searchCategories(
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

        PageResponseDTO<CategoryResponseDTO> response = categoryService.searchCategories(searchDTO);
        return ResponseEntity.ok(response);
    }
}
