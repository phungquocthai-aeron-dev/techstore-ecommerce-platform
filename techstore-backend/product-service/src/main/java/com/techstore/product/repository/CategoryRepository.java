package com.techstore.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techstore.product.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Tìm kiếm category theo tên hoặc mô tả
     */
    @Query("SELECT c FROM Category c WHERE " + "(:keyword IS NULL OR "
            + "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:categoryType IS NULL OR c.categoryType = :categoryType)")
    Page<Category> searchCategories(
            @Param("keyword") String keyword, @Param("categoryType") String categoryType, Pageable pageable);

    /**
     * Lấy category theo categoryType
     */
    Page<Category> findByCategoryType(String categoryType, Pageable pageable);
}
