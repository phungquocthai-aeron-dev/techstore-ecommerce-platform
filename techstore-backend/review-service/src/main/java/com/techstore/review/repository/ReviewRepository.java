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

    @Query(
            """
			SELECT r FROM Review r
			WHERE (:productId IS NULL OR r.productId = :productId)
			AND (:customerId IS NULL OR r.customerId = :customerId)
			AND (:rating IS NULL OR r.rating = :rating)
			AND (:status IS NULL OR r.status = :status)
			AND (:hasReply IS NULL OR
				(:hasReply = true AND r.reply IS NOT NULL AND r.reply.status = 'ACTIVE') OR
				(:hasReply = false AND (r.reply IS NULL OR r.reply.status != 'ACTIVE')))
			AND (:keyword IS NULL OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
		""")
    Page<Review> searchReviews(
            @Param("productId") Long productId,
            @Param("customerId") Long customerId,
            @Param("rating") Integer rating,
            @Param("status") String status,
            @Param("hasReply") Boolean hasReply,
            @Param("keyword") String keyword,
            Pageable pageable);
}
