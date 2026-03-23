package com.techstore.review.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.techstore.event.dto.PostEvent;
import com.techstore.review.client.ModerationClient;
import com.techstore.review.client.OrderServiceClient;
import com.techstore.review.client.ProductServiceClient;
import com.techstore.review.client.UserServiceClient;
import com.techstore.review.constant.ReviewStatus;
import com.techstore.review.dto.request.CreateReplyRequest;
import com.techstore.review.dto.request.CreateReviewRequest;
import com.techstore.review.dto.request.ModerationRequest;
import com.techstore.review.dto.request.ReplySearchRequest;
import com.techstore.review.dto.request.ReviewSearchRequest;
import com.techstore.review.dto.request.UpdateReplyRequest;
import com.techstore.review.dto.request.UpdateReviewRequest;
import com.techstore.review.dto.response.CustomerResponse;
import com.techstore.review.dto.response.PageResponse;
import com.techstore.review.dto.response.ReplyResponse;
import com.techstore.review.dto.response.ReviewResponse;
import com.techstore.review.dto.response.StaffResponse;
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
    private final UserServiceClient userClient;
    private final ModerationClient moderationClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "HIDDEN", "SPAM", "TOXIC");

    public PageResponse<ReviewResponse> searchReviews(ReviewSearchRequest req) {

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(req.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC, req.getSortBy());
        PageRequest pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        Page<Review> reviewPage = reviewRepo.searchReviews(
                req.getProductId(),
                req.getCustomerId(),
                req.getRating(),
                req.getStatus(),
                req.getHasReply(),
                req.getKeyword(),
                pageable);

        List<ReviewResponse> responses =
                reviewPage.getContent().stream().map(mapper::toResponse).toList();

        if (responses.isEmpty()) {
            return buildPageResponse(responses, reviewPage);
        }

        // Enrich với user info (tái dùng logic sẵn có)
        List<Long> customerIds =
                responses.stream().map(ReviewResponse::getCustomerId).distinct().toList();

        List<Long> staffIds = responses.stream()
                .map(ReviewResponse::getReply)
                .filter(r -> r != null)
                .map(ReplyResponse::getStaffId)
                .distinct()
                .toList();

        Map<Long, CustomerResponse> customerMap = userClient.getCustomerByIds(customerIds).getResult().stream()
                .collect(Collectors.toMap(CustomerResponse::getId, c -> c));

        Map<Long, StaffResponse> staffMap = new HashMap<>();
        if (!staffIds.isEmpty()) {
            staffMap = userClient.getStaffByIds(staffIds).getResult().stream()
                    .collect(Collectors.toMap(StaffResponse::getId, s -> s));
        }

        final Map<Long, StaffResponse> finalStaffMap = staffMap;
        for (ReviewResponse review : responses) {
            review.setCustomer(customerMap.get(review.getCustomerId()));
            if (review.getReply() != null) {
                review.getReply().setStaff(finalStaffMap.get(review.getReply().getStaffId()));
            }
        }

        return buildPageResponse(responses, reviewPage);
    }

    // Helper tránh lặp code
    private <T> PageResponse<T> buildPageResponse(List<T> content, Page<?> page) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    public PageResponse<ReviewResponse> getAllReviews(int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviewPage = reviewRepo.findAllActive(pageable);
        List<ReviewResponse> responses =
                reviewPage.getContent().stream().map(mapper::toResponse).toList();

        if (responses.isEmpty()) {
            return buildPageResponse(responses, reviewPage);
        }

        List<Long> customerIds =
                responses.stream().map(ReviewResponse::getCustomerId).distinct().toList();

        List<Long> staffIds = responses.stream()
                .map(ReviewResponse::getReply)
                .filter(r -> r != null)
                .map(ReplyResponse::getStaffId)
                .distinct()
                .toList();

        Map<Long, CustomerResponse> customerMap = userClient.getCustomerByIds(customerIds).getResult().stream()
                .collect(Collectors.toMap(CustomerResponse::getId, c -> c));

        Map<Long, StaffResponse> staffMap = new HashMap<>();
        if (!staffIds.isEmpty()) {
            staffMap = userClient.getStaffByIds(staffIds).getResult().stream()
                    .collect(Collectors.toMap(StaffResponse::getId, s -> s));
        }

        final Map<Long, StaffResponse> finalStaffMap = staffMap;
        for (ReviewResponse review : responses) {
            review.setCustomer(customerMap.get(review.getCustomerId()));
            if (review.getReply() != null) {
                review.getReply().setStaff(finalStaffMap.get(review.getReply().getStaffId()));
            }
        }

        return buildPageResponse(responses, reviewPage);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public PageResponse<ReplyResponse> searchReplies(ReplySearchRequest req) {

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(req.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC, "createdAt");
        PageRequest pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        Page<Reply> replyPage = replyRepo.searchReplies(req.getStaffId(), req.getStatus(), req.getKeyword(), pageable);

        List<ReplyResponse> responses =
                replyPage.getContent().stream().map(mapper::toReplyResponse).toList();

        if (!responses.isEmpty()) {
            List<Long> staffIds =
                    responses.stream().map(ReplyResponse::getStaffId).distinct().toList();

            Map<Long, StaffResponse> staffMap = userClient.getStaffByIds(staffIds).getResult().stream()
                    .collect(Collectors.toMap(StaffResponse::getId, s -> s));

            responses.forEach(r -> r.setStaff(staffMap.get(r.getStaffId())));
        }

        return buildPageResponse(responses, replyPage);
    }

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

        ModerationRequest modReq = new ModerationRequest();
        modReq.setContent(req.getContent());

        var modResult = moderationClient.predict(modReq).getResult();

        String status = mapModerationToStatus(modResult.getLabel());

        Review review = Review.builder()
                .content(req.getContent())
                .rating(req.getRating())
                .status(status)
                .productId(product.getId())
                .variantId(orderDetail.getVariantId())
                .orderDetailId(orderDetail.getId())
                .customerId(getCurrentUserId())
                .build();

        orderClient.markOrderDetailReviewed(orderDetail.getId());

        String content = buildReviewContent(modResult.getLabel());
        PostEvent event = PostEvent.builder()
                .title("Cập nhật trạng thái đơn hàng")
                .content(content)
                .userId(getCurrentUserId().toString())
                .build();

        kafkaTemplate.send("post-delivery", event);

        return mapper.toResponse(reviewRepo.save(review));
    }

    // ===================== UPDATE REVIEW =====================

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ReviewResponse updateReview(Long id, UpdateReviewRequest req) {

        Review review = getReviewAndCheckPermission(id);

        review.setRating(req.getRating());

        if (req.getContent() != null
                && !req.getContent().isBlank()
                && !req.getContent().equals(review.getContent())) {

            review.setContent(req.getContent());

            ModerationRequest modReq = new ModerationRequest();
            modReq.setContent(req.getContent());

            var modResult = moderationClient.predict(modReq).getResult();

            review.setStatus(mapModerationToStatus(modResult.getLabel()));
        }

        return mapper.toResponse(reviewRepo.save(review));
    }

    // ===================== DELETE REVIEW (SOFT) =====================

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public void deleteReview(Long id) {

        Review review = getReviewAndCheckPermission(id);
        review.setStatus(ReviewStatus.HIDDEN.name());
        reviewRepo.save(review);
    }

    // ===================== UPDATE STATUS (STAFF/ADMIN) =====================

    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public ReviewResponse updateReviewStatus(Long id, String status) {

        if (!VALID_STATUSES.contains(status)) {
            throw new AppException(ErrorCode.INVALID_REVIEW_STATUS);
        }

        Review review = reviewRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        review.setStatus(status);

        return mapper.toResponse(reviewRepo.save(review));
    }

    // ===================== GET REVIEWS =====================

    public PageResponse<ReviewResponse> getReviews(Long productId, Integer rating, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviewPage = reviewRepo.findActiveByProductId(productId, rating, pageable);
        List<ReviewResponse> responses =
                reviewPage.getContent().stream().map(mapper::toResponse).toList();

        if (responses.isEmpty()) {
            return PageResponse.<ReviewResponse>builder()
                    .content(responses)
                    .page(reviewPage.getNumber())
                    .size(reviewPage.getSize())
                    .totalElements(reviewPage.getTotalElements())
                    .totalPages(reviewPage.getTotalPages())
                    .last(reviewPage.isLast())
                    .build();
        }

        // ================= COLLECT IDS =================

        List<Long> customerIds =
                responses.stream().map(ReviewResponse::getCustomerId).distinct().toList();

        List<Long> staffIds = responses.stream()
                .map(ReviewResponse::getReply)
                .filter(r -> r != null)
                .map(ReplyResponse::getStaffId)
                .distinct()
                .toList();

        // ================= CALL USER SERVICE =================

        Map<Long, CustomerResponse> customerMap = userClient.getCustomerByIds(customerIds).getResult().stream()
                .collect(Collectors.toMap(CustomerResponse::getId, c -> c));

        Map<Long, StaffResponse> staffMap = new HashMap<>();

        if (!staffIds.isEmpty()) {
            staffMap = userClient.getStaffByIds(staffIds).getResult().stream()
                    .collect(Collectors.toMap(StaffResponse::getId, s -> s));
        }

        // ================= MAP BACK =================

        for (ReviewResponse review : responses) {

            review.setCustomer(customerMap.get(review.getCustomerId()));

            if (review.getReply() != null) {
                review.getReply().setStaff(staffMap.get(review.getReply().getStaffId()));
            }
        }

        return PageResponse.<ReviewResponse>builder()
                .content(responses)
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .build();
    }

    // ===================== REPLY =====================

    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public ReplyResponse reply(Long reviewId, CreateReplyRequest req) {

        Review review = reviewRepo.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!"ACTIVE".equals(review.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REVIEW_STATUS);
        }

        if (replyRepo.existsByReviewId(reviewId)) {
            throw new AppException(ErrorCode.REPLY_ALREADY_EXISTED);
        }

        ModerationRequest modReq = new ModerationRequest();
        modReq.setContent(req.getContent());

        var modResult = moderationClient.predict(modReq).getResult();

        String status = mapModerationToStatus(modResult.getLabel());

        Reply reply = Reply.builder()
                .content(req.getContent())
                .status(status)
                .staffId(getCurrentUserId())
                .review(review)
                .build();

        return mapper.toReplyResponse(replyRepo.save(reply));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public ReplyResponse updateReply(Long id, UpdateReplyRequest req) {

        Reply reply = replyRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.REPLY_NOT_FOUND));

        reply.setContent(req.getContent());

        ModerationRequest modReq = new ModerationRequest();
        modReq.setContent(req.getContent());

        var modResult = moderationClient.predict(modReq).getResult();

        String status = mapModerationToStatus(modResult.getLabel());
        reply.setStatus(status);

        return mapper.toReplyResponse(reply);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public void deleteReply(Long id) {

        Reply reply = replyRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.REPLY_NOT_FOUND));

        reply.setStatus(ReviewStatus.HIDDEN.name());
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

    private String mapModerationToStatus(String label) {
        return switch (label) {
            case "Valid" -> "ACTIVE";
            case "Spam" -> "SPAM";
            case "Toxic" -> "TOXIC";
            default -> "ACTIVE";
        };
    }

    private String buildReviewContent(String label) {
        return switch (label) {
            case "Spam", "Toxic" -> "Đánh giá của bạn đã được ghi nhận nhưng cần kiểm duyệt trước khi hiển thị.";
            default -> "Đánh giá của bạn đã được đăng thành công.";
        };
    }
}
