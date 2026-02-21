package com.techstore.review.service;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.techstore.review.client.OrderServiceClient;
import com.techstore.review.client.ProductServiceClient;
import com.techstore.review.dto.request.CreateReplyRequest;
import com.techstore.review.dto.request.CreateReviewRequest;
import com.techstore.review.dto.request.UpdateReplyRequest;
import com.techstore.review.dto.request.UpdateReviewRequest;
import com.techstore.review.dto.response.PageResponse;
import com.techstore.review.dto.response.ReplyResponse;
import com.techstore.review.dto.response.ReviewResponse;
import com.techstore.review.entity.Reply;
import com.techstore.review.entity.Review;
import com.techstore.review.exception.AppException;
import com.techstore.review.exception.ErrorCode;
import com.techstore.review.mapper.ReviewMapper;
import com.techstore.review.repository.ReplyRepository;
import com.techstore.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ReplyRepository replyRepo;
    private final ReviewMapper mapper;

    private final OrderServiceClient orderClient;
    private final ProductServiceClient productClient;

    // ===================== CREATE REVIEW =====================

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ReviewResponse createReview(CreateReviewRequest req) {

        if (req.getRating() < 1 || req.getRating() > 5) {
            throw new AppException(ErrorCode.INVALID_RATING);
        }

        if (reviewRepo.existsByOrderDetailId(req.getOrderDetailId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTED);
        }

        var orderDetail = orderClient.getOrderDetailById(req.getOrderDetailId()).getResult();

        if (!"ACTIVE".equals(orderDetail.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_COMPLETED);
        }

        var product =
                productClient.getProductByVariantId(orderDetail.getVariantId()).getResult();

        Review review = Review.builder()
                .content(req.getContent())
                .rating(req.getRating())
                .status("ACTIVE")
                .productId(product.getId())
                .variantId(orderDetail.getVariantId())
                .orderDetailId(orderDetail.getId())
                .customerId(getCurrentUserId())
                .build();

        return mapper.toResponse(reviewRepo.save(review));
    }

    // ===================== UPDATE REVIEW =====================

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ReviewResponse updateReview(Long id, UpdateReviewRequest req) {

        Review review = getReviewAndCheckPermission(id);

        review.setContent(req.getContent());
        review.setRating(req.getRating());

        return mapper.toResponse(reviewRepo.save(review));
    }

    // ===================== DELETE REVIEW (SOFT) =====================

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public void deleteReview(Long id) {

        Review review = getReviewAndCheckPermission(id);
        review.setStatus("DELETED");
        reviewRepo.save(review);
    }

    // ===================== GET REVIEWS =====================

    public PageResponse<ReviewResponse> getReviews(Long productId, Integer rating, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviewPage = reviewRepo.findActiveByProductId(productId, rating, pageable);

        return PageResponse.<ReviewResponse>builder()
                .content(
                        reviewPage.getContent().stream().map(mapper::toResponse).toList())
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .build();
    }

    // ===================== REPLY =====================

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ReplyResponse reply(Long reviewId, CreateReplyRequest req) {

        Review review = reviewRepo.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!"ACTIVE".equals(review.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REVIEW_STATUS);
        }

        if (replyRepo.existsByReviewId(reviewId)) {
            throw new AppException(ErrorCode.REPLY_ALREADY_EXISTED);
        }

        Reply reply = Reply.builder()
                .content(req.getContent())
                .status("ACTIVE")
                .staffId(getCurrentUserId())
                .review(review)
                .build();

        return mapper.toReplyResponse(replyRepo.save(reply));
    }

    public ReplyResponse updateReply(Long id, UpdateReplyRequest req) {

        Reply reply = replyRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.REPLY_NOT_FOUND));

        reply.setContent(req.getContent());

        return mapper.toReplyResponse(reply);
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public void deleteReply(Long id) {

        Reply reply = replyRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.REPLY_NOT_FOUND));

        reply.setStatus("DELETED");
        replyRepo.save(reply);
    }

    // ===================== HELPER =====================

    private Review getReviewAndCheckPermission(Long id) {

        Review review = reviewRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (isAdmin()) return review;

        if (!review.getCustomerId().equals(getCurrentUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return review;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return Long.valueOf(jwt.getSubject());
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
