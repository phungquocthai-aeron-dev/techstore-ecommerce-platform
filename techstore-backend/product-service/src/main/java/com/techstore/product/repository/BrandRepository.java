package com.techstore.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techstore.product.entity.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Tìm kiếm brand theo tên
     */
    @Query("SELECT b FROM Brand b WHERE "
            + "(:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:status IS NULL OR b.status = :status)")
    Page<Brand> searchBrands(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);

    /**
     * Lấy brand theo status
     */
    Page<Brand> findByStatus(String status, Pageable pageable);
}
