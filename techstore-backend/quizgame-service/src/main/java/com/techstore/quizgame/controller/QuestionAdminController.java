package com.techstore.quizgame.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.techstore.quizgame.dto.request.AnswerRequestDTO;
import com.techstore.quizgame.dto.request.QuestionRequestDTO;
import com.techstore.quizgame.dto.response.AnswerDetailResponseDTO;
import com.techstore.quizgame.dto.response.ApiResponse;
import com.techstore.quizgame.dto.response.QuestionDetailResponseDTO;
import com.techstore.quizgame.service.QuestionAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
public class QuestionAdminController {

    private final QuestionAdminService questionAdminService;

    // ===== QUESTION CRUD =====

    /** GET /api/admin/questions — Tất cả câu hỏi */
    @GetMapping
    public ApiResponse<List<QuestionDetailResponseDTO>> getAllQuestions() {
        return ApiResponse.<List<QuestionDetailResponseDTO>>builder()
                .result(questionAdminService.getAllQuestions())
                .build();
    }

    /** GET /api/admin/questions?topicId=1 — Lọc theo chủ đề */
    @GetMapping(params = "topicId")
    public ApiResponse<List<QuestionDetailResponseDTO>> getQuestionsByTopic(@RequestParam Long topicId) {
        return ApiResponse.<List<QuestionDetailResponseDTO>>builder()
                .result(questionAdminService.getQuestionsByTopic(topicId))
                .build();
    }

    /** GET /api/admin/questions/{id} — Chi tiết câu hỏi */
    @GetMapping("/{id}")
    public ApiResponse<QuestionDetailResponseDTO> getQuestionById(@PathVariable Long id) {
        return ApiResponse.<QuestionDetailResponseDTO>builder()
                .result(questionAdminService.getQuestionById(id))
                .build();
    }

    /** POST /api/admin/questions — Tạo câu hỏi kèm đáp án */
    @PostMapping
    public ApiResponse<QuestionDetailResponseDTO> createQuestion(@Valid @RequestBody QuestionRequestDTO request) {
        return ApiResponse.<QuestionDetailResponseDTO>builder()
                .result(questionAdminService.createQuestion(request))
                .build();
    }

    /** PUT /api/admin/questions/{id} — Cập nhật toàn bộ câu hỏi */
    @PutMapping("/{id}")
    public ApiResponse<QuestionDetailResponseDTO> updateQuestion(
            @PathVariable Long id, @Valid @RequestBody QuestionRequestDTO request) {
        return ApiResponse.<QuestionDetailResponseDTO>builder()
                .result(questionAdminService.updateQuestion(id, request))
                .build();
    }

    /** DELETE /api/admin/questions/{id} — Xóa câu hỏi */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteQuestion(@PathVariable Long id) {
        questionAdminService.deleteQuestion(id);
        return ApiResponse.<String>builder().result("Xóa câu hỏi thành công").build();
    }

    // ===== ANSWER CRUD (nested resource) =====

    /** POST /api/admin/questions/{questionId}/answers — Thêm đáp án */
    @PostMapping("/{questionId}/answers")
    public ApiResponse<AnswerDetailResponseDTO> addAnswer(
            @PathVariable Long questionId, @Valid @RequestBody AnswerRequestDTO request) {
        return ApiResponse.<AnswerDetailResponseDTO>builder()
                .result(questionAdminService.addAnswer(questionId, request))
                .build();
    }

    /** PUT /api/admin/questions/{questionId}/answers/{answerId} — Sửa đáp án */
    @PutMapping("/{questionId}/answers/{answerId}")
    public ApiResponse<AnswerDetailResponseDTO> updateAnswer(
            @PathVariable Long questionId, @PathVariable Long answerId, @Valid @RequestBody AnswerRequestDTO request) {
        return ApiResponse.<AnswerDetailResponseDTO>builder()
                .result(questionAdminService.updateAnswer(questionId, answerId, request))
                .build();
    }

    /** DELETE /api/admin/questions/{questionId}/answers/{answerId} — Xóa đáp án */
    @DeleteMapping("/{questionId}/answers/{answerId}")
    public ApiResponse<String> deleteAnswer(@PathVariable Long questionId, @PathVariable Long answerId) {
        questionAdminService.deleteAnswer(questionId, answerId);
        return ApiResponse.<String>builder().result("Xóa đáp án thành công").build();
    }
}
