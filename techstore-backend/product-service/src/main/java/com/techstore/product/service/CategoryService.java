package com.techstore.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.product.dto.request.CategoryCreateRequestDTO;
import com.techstore.product.dto.request.CategorySearchRequestDTO;
import com.techstore.product.dto.request.CategoryUpdateRequestDTO;
import com.techstore.product.dto.response.CategoryResponseDTO;
import com.techstore.product.dto.response.PageResponseDTO;
import com.techstore.product.entity.Category;
import com.techstore.product.exception.AppException;
import com.techstore.product.exception.ErrorCode;
import com.techstore.product.mapper.CategoryMapper;
import com.techstore.product.repository.CategoryRepository;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * Thêm category mới (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponseDTO createCategory(CategoryCreateRequestDTO requestDTO) {
        Category category = new Category();
        category.setName(requestDTO.getName());
        category.setCategoryType(requestDTO.getCategoryType());
        category.setPcComponentType(requestDTO.getPcComponentType());
        category.setDescription(requestDTO.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(savedCategory);
    }

    /**
     * Cập nhật category (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateRequestDTO requestDTO) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (requestDTO.getName() != null) {
            category.setName(requestDTO.getName());
        }
        if (requestDTO.getCategoryType() != null) {
            category.setCategoryType(requestDTO.getCategoryType());
        }
        if (requestDTO.getPcComponentType() != null) {
            category.setPcComponentType(requestDTO.getPcComponentType());
        }
        if (requestDTO.getDescription() != null) {
            category.setDescription(requestDTO.getDescription());
        }

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(updatedCategory);
    }

    /**
     * Xóa category (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }

    /**
     * Lấy category theo ID
     */
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toResponseDTO(category);
    }

    /**
     * Lấy tất cả categories (phân trang)
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryResponseDTO> getAllCategories(
            int page, int size, String sortBy, String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryResponseDTO> content = categoryPage.getContent().stream()
                .map(categoryMapper::toResponseDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast());
    }

    /**
     * Lấy categories theo categoryType
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryResponseDTO> getCategoriesByType(
            String categoryType, int page, int size, String sortBy, String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categoryPage = categoryRepository.findByCategoryType(categoryType, pageable);

        List<CategoryResponseDTO> content = categoryPage.getContent().stream()
                .map(categoryMapper::toResponseDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast());
    }

    /**
     * Tìm kiếm categories
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryResponseDTO> searchCategories(CategorySearchRequestDTO searchDTO) {
        Sort sort = searchDTO.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(searchDTO.getSortBy()).ascending()
                : Sort.by(searchDTO.getSortBy()).descending();

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        Page<Category> categoryPage =
                categoryRepository.searchCategories(searchDTO.getKeyword(), searchDTO.getCategoryType(), pageable);

        List<CategoryResponseDTO> content = categoryPage.getContent().stream()
                .map(categoryMapper::toResponseDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast());
    }
}
