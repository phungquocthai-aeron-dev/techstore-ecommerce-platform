package com.techstore.notification.service;

import java.time.Instant;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.techstore.event.dto.PostEvent;
import com.techstore.notification.client.UserServiceClient;
import com.techstore.notification.dto.request.PostRequest;
import com.techstore.notification.dto.response.CustomerResponse;
import com.techstore.notification.dto.response.PageResponse;
import com.techstore.notification.dto.response.PostResponse;
import com.techstore.notification.entity.Post;
import com.techstore.notification.mapper.PostMapper;
import com.techstore.notification.repository.PostRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {
    DateTimeFormatter dateTimeFormatter;
    PostRepository postRepository;
    PostMapper postMapper;
    UserServiceClient userServiceClient;

    public PostResponse createPost(PostRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .userId(authentication.getName())
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .isRead(false)
                .build();

        post = postRepository.save(post);
        return postMapper.toPostResponse(post);
    }

    public PostResponse createPostInternal(PostEvent request) {

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .userId(request.getUserId())
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .isRead(false)
                .build();

        post = postRepository.save(post);
        return postMapper.toPostResponse(post);
    }

    //    public PageResponse<PostResponse> getMyPosts(int page, int size) {
    //        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //        String userId = authentication.getName();
    //
    //        CustomerResponse userProfile = null;
    //
    //        try {
    //            userProfile =
    //                    userServiceClient.getCustomerById(Long.valueOf(userId)).getResult();
    //        } catch (Exception e) {
    //            log.error("Error while getting user profile", e);
    //        }
    //        Sort sort = Sort.by("createdDate").descending();
    //
    //        Pageable pageable = PageRequest.of(page - 1, size, sort);
    //        var pageData = postRepository.findAllByUserId(userId, pageable);
    //
    //        String username = userProfile != null ? userProfile.getFullName() : null;
    //        var postList = pageData.getContent().stream()
    //                .map(post -> {
    //                    var postResponse = postMapper.toPostResponse(post);
    //                    postResponse.setCreatedDate(dateTimeFormatter.format(post.getCreatedDate()));
    //                    postResponse.setUserId(username);
    //                    return postResponse;
    //                })
    //                .toList();
    //
    //        return PageResponse.<PostResponse>builder()
    //                .currentPage(page)
    //                .pageSize(pageData.getSize())
    //                .totalPages(pageData.getTotalPages())
    //                .totalElements(pageData.getTotalElements())
    //                .data(postList)
    //                .build();
    //    }

    public PageResponse<PostResponse> getMyPosts(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        CustomerResponse userProfile = null;
        try {
            userProfile =
                    userServiceClient.getCustomerById(Long.valueOf(userId)).getResult();
        } catch (Exception e) {
            log.error("Error while getting user profile", e);
        }

        Sort sort = Sort.by("createdDate").descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        var pageData = postRepository.findAllByUserIdOrGlobal(userId, pageable);

        String username = userProfile != null ? userProfile.getFullName() : null;

        var postList = pageData.getContent().stream()
                .map(post -> {
                    var postResponse = postMapper.toPostResponse(post);
                    postResponse.setCreatedDate(dateTimeFormatter.format(post.getCreatedDate()));
                    postResponse.setUserId("0".equals(post.getUserId()) ? "All" : username);
                    return postResponse;
                })
                .toList();

        return PageResponse.<PostResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(postList)
                .build();
    }

    public void markAsRead(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // đảm bảo user chỉ update post của mình
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        post.setRead(true);
        post.setModifiedDate(Instant.now());

        postRepository.save(post);
    }

    public void markAllAsRead() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        var posts = postRepository.findAllByUserIdAndIsReadFalse(userId);

        posts.forEach(post -> {
            post.setRead(true);
            post.setModifiedDate(Instant.now());
        });

        postRepository.saveAll(posts);
    }
}
