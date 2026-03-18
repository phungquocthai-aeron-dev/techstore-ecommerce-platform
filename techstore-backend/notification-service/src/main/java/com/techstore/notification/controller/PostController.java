package com.techstore.notification.controller;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.event.dto.PostEvent;
import com.techstore.notification.dto.request.PostRequest;
import com.techstore.notification.dto.response.ApiResponse;
import com.techstore.notification.dto.response.PageResponse;
import com.techstore.notification.dto.response.PostResponse;
import com.techstore.notification.service.PostService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
    PostService postService;

    @PostMapping("/create")
    ApiResponse<PostResponse> createPost(@RequestBody PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(request))
                .build();
    }

    @KafkaListener(topics = "post-delivery")
    ApiResponse<PostResponse> listenPostDelivery(PostEvent event) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPostInternal(event))
                .build();
    }

    @GetMapping("/my-posts")
    ApiResponse<PageResponse<PostResponse>> myPosts(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getMyPosts(page, size))
                .build();
    }

    @PutMapping("/mark-as-read")
    ApiResponse<Void> markAsRead(@RequestParam String postId) {
        postService.markAsRead(postId);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/mark-all-as-read")
    ApiResponse<Void> markAllAsRead() {
        postService.markAllAsRead();
        return ApiResponse.<Void>builder().build();
    }
}
