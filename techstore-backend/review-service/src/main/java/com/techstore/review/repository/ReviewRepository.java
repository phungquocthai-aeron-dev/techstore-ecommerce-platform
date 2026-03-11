package com.techstore.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderDetailId(Long orderDetailId);

    @Query(
            """
		SELECT r FROM Review r
		WHERE r.productId = :productId
		AND r.status = 'ACTIVE'
		AND (:rating IS NULL OR r.rating = :rating)
	""")
    Page<Review> findActiveByProductId(
            @Param("productId") Long productId, @Param("rating") Integer rating, Pageable pageable);
}
