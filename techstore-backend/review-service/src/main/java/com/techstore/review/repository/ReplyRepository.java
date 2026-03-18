package com.techstore.review.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.review.entity.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Optional<Reply> findByReviewId(Long reviewId);

    boolean existsByReviewId(Long reviewId);

    @Query(
            """
			SELECT r FROM Reply r
			WHERE (:staffId IS NULL OR r.staffId = :staffId)
			AND (:status IS NULL OR r.status = :status)
			AND (:keyword IS NULL OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
		""")
    Page<Reply> searchReplies(
            @Param("staffId") Long staffId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable);
}
