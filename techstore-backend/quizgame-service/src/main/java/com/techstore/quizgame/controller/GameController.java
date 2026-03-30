package com.techstore.quizgame.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.techstore.quizgame.dto.request.StartGameRequestDTO;
import com.techstore.quizgame.dto.request.SubmitAnswerRequestDTO;
import com.techstore.quizgame.dto.response.ApiResponse;
import com.techstore.quizgame.dto.response.GameResultResponseDTO;
import com.techstore.quizgame.dto.response.StartGameResponseDTO;
import com.techstore.quizgame.service.GameService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    /**
     * POST /api/game/start
     * Bắt đầu lượt chơi mới, trả về 10 câu hỏi random
     */
    @PostMapping("/start")
    public ApiResponse<StartGameResponseDTO> startGame(@RequestBody StartGameRequestDTO request) {
        return ApiResponse.<StartGameResponseDTO>builder()
                .result(gameService.startGame(request))
                .build();
    }

    /**
     * POST /api/game/submit
     * Nộp đáp án, nhận kết quả và điểm
     */
    @PostMapping("/submit")
    public ApiResponse<GameResultResponseDTO> submitGame(@RequestBody SubmitAnswerRequestDTO request) {
        return ApiResponse.<GameResultResponseDTO>builder()
                .result(gameService.submitGame(request))
                .build();
    }

    /**
     * GET /api/game/daily-info?userId=1
     * Kiểm tra thông tin lượt chơi hôm nay
     */
    @GetMapping("/daily-info")
    public ApiResponse<Map<String, Object>> getDailyInfo(@RequestParam Long userId) {
        return ApiResponse.<Map<String, Object>>builder()
                .result(gameService.getDailyPlayInfo(userId))
                .build();
    }

    /**
     * GET /api/game/points?userId=1
     * Lấy tổng điểm của user
     */
    @GetMapping("/points")
    public ApiResponse<Map<String, Object>> getUserPoints(@RequestParam Long userId) {
        return ApiResponse.<Map<String, Object>>builder()
                .result(gameService.getUserPoints(userId))
                .build();
    }
}
