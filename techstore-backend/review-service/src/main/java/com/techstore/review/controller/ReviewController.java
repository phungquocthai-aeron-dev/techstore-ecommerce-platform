package com.techstore.review.controller;

import org.springframework.web.bind.annotation.*;

import com.techstore.review.dto.request.*;
import com.techstore.review.dto.response.*;
import com.techstore.review.dto.response.ApiResponse;
import com.techstore.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ================= CREATE REVIEW =================
    @PostMapping
    public ApiResponse<ReviewResponse> create(@RequestBody CreateReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.createReview(request))
                .message("Create review successfully")
                .build();
    }

    // ================= UPDATE REVIEW =================
    @PutMapping("/{id}")
    public ApiResponse<ReviewResponse> update(@PathVariable Long id, @RequestBody UpdateReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.updateReview(id, request))
                .message("Update review successfully")
                .build();
    }

    // ================= DELETE REVIEW =================
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ApiResponse.<Void>builder().message("Delete review successfully").build();
    }

    // ================= GET REVIEWS (PAGINATION) =================
    @GetMapping
    public ApiResponse<PageResponse<ReviewResponse>> getReviews(
            @RequestParam Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .result(reviewService.getReviews(productId, rating, page, size))
                .build();
    }

    // ================= REPLY =================
    @PostMapping("/{reviewId}/reply")
    public ApiResponse<ReplyResponse> reply(@PathVariable Long reviewId, @RequestBody CreateReplyRequest request) {
        return ApiResponse.<ReplyResponse>builder()
                .result(reviewService.reply(reviewId, request))
                .message("Reply successfully")
                .build();
    }

    @PutMapping("/reply/{id}")
    public ApiResponse<ReplyResponse> updateReply(@PathVariable Long id, @RequestBody UpdateReplyRequest request) {
        return ApiResponse.<ReplyResponse>builder()
                .result(reviewService.updateReply(id, request))
                .message("Update reply successfully")
                .build();
    }

    @DeleteMapping("/reply/{id}")
    public ApiResponse<Void> deleteReply(@PathVariable Long id) {
        reviewService.deleteReply(id);
        return ApiResponse.<Void>builder().message("Delete reply successfully").build();
    }
}
