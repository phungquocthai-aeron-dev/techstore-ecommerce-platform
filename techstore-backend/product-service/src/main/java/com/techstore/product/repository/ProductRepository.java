package com.techstore.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techstore.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query(
            """
			SELECT DISTINCT p FROM Product p
			LEFT JOIN FETCH p.images
			LEFT JOIN FETCH p.specs
			LEFT JOIN FETCH p.variants
			WHERE p.id = :id
		""")
    Optional<Product> findDetailById(@Param("id") Long id);

    /**
     * Lấy danh sách sản phẩm theo category name
     */
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
    Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    /**
     * Lấy n sản phẩm mới nhất
     */
    @Query("""
			SELECT p FROM Product p
			WHERE p.status = 'ACTIVE'
			ORDER BY p.id DESC
		""")
    List<Product> findLatestProducts(Pageable pageable);

    /**
     * Tìm kiếm sản phẩm theo tên sản phẩm, tên danh mục, tên thương hiệu
     * Có hỗ trợ lọc theo brand và khoảng giá
     */
    @Query(
            """
			SELECT p FROM Product p
			WHERE p.status = 'ACTIVE'
			AND (
				:keyword IS NULL OR
				LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
				LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
				LOWER(p.brand.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:brandName IS NULL OR p.brand.name = :brandName)
			AND (:minPrice IS NULL OR p.basePrice >= :minPrice)
			AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)
		""")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("brandName") String brandName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.variants v WHERE v.id = :variantId")
    Optional<Product> findByVariantId(@Param("variantId") Long variantId);
}
