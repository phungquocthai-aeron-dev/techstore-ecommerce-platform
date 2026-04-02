package com.techstore.quizgame.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.techstore.quizgame.dto.request.TopicRequestDTO;
import com.techstore.quizgame.dto.response.ApiResponse;
import com.techstore.quizgame.dto.response.TopicResponseDTO;
import com.techstore.quizgame.service.TopicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /** GET /api/topics — Lấy tất cả chủ đề */
    @GetMapping
    public ApiResponse<List<TopicResponseDTO>> getAllTopics() {
        return ApiResponse.<List<TopicResponseDTO>>builder()
                .result(topicService.getAllTopics())
                .build();
    }

    /** GET /api/topics/{id} — Chi tiết 1 chủ đề */
    @GetMapping("/{id}")
    public ApiResponse<TopicResponseDTO> getTopicById(@PathVariable Long id) {
        return ApiResponse.<TopicResponseDTO>builder()
                .result(topicService.getTopicById(id))
                .build();
    }

    /** POST /api/topics — Tạo chủ đề mới */
    @PostMapping
    public ApiResponse<TopicResponseDTO> createTopic(@Valid @RequestBody TopicRequestDTO request) {
        return ApiResponse.<TopicResponseDTO>builder()
                .result(topicService.createTopic(request))
                .build();
    }

    /** PUT /api/topics/{id} — Cập nhật chủ đề */
    @PutMapping("/{id}")
    public ApiResponse<TopicResponseDTO> updateTopic(
            @PathVariable Long id, @Valid @RequestBody TopicRequestDTO request) {
        return ApiResponse.<TopicResponseDTO>builder()
                .result(topicService.updateTopic(id, request))
                .build();
    }

    /** DELETE /api/topics/{id} — Xóa chủ đề */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ApiResponse.<String>builder().result("Xóa chủ đề thành công").build();
    }
}
