package com.techstore.review.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.review.entity.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Optional<Reply> findByReviewId(Long reviewId);

    boolean existsByReviewId(Long reviewId);
}
