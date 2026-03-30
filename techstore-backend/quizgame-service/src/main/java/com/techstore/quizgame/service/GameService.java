package com.techstore.quizgame.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.quizgame.dto.request.StartGameRequestDTO;
import com.techstore.quizgame.dto.request.SubmitAnswerRequestDTO;
import com.techstore.quizgame.dto.request.UserAnswerDTO;
import com.techstore.quizgame.dto.response.*;
import com.techstore.quizgame.entity.*;
import com.techstore.quizgame.exception.AppException;
import com.techstore.quizgame.exception.ErrorCode;
import com.techstore.quizgame.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserDailyPlayRepository userDailyPlayRepository;
    private final UserPointRepository userPointRepository;

    @Value("${app.game.max-plays-per-day:3}")
    private int maxPlaysPerDay;

    @Value("${app.game.questions-per-game:10}")
    private int questionsPerGame;

    /**
     * Bắt đầu game: kiểm tra lượt chơi và trả về 10 câu hỏi random
     */
    @Transactional
    public StartGameResponseDTO startGame(StartGameRequestDTO request) {
        Long userId = request.getUserId();
        LocalDate today = LocalDate.now();

        // 1. Lấy hoặc tạo record lượt chơi hôm nay
        UserDailyPlay dailyPlay = userDailyPlayRepository
                .findByUserIdAndPlayDate(userId, today)
                .orElseGet(() -> {
                    // Tạo record mới nếu user chưa chơi hôm nay
                    UserDailyPlay newPlay = UserDailyPlay.builder()
                            .userId(userId)
                            .playDate(today)
                            .playCount(0)
                            .build();
                    return userDailyPlayRepository.save(newPlay);
                });

        // 2. Kiểm tra đã đủ lượt chưa
        if (dailyPlay.getPlayCount() >= maxPlaysPerDay) {
            throw new AppException(ErrorCode.DAILY_PLAY_LIMIT_EXCEEDED);
        }

        // 3. Lấy random N câu hỏi từ DB
        List<Question> questions = questionRepository.findRandomQuestions(PageRequest.of(0, questionsPerGame));

        if (questions.size() < questionsPerGame) {
            log.warn("Hệ thống chỉ có {} câu hỏi, cần {}", questions.size(), questionsPerGame);
            throw new AppException(ErrorCode.NOT_ENOUGH_QUESTIONS);
        }

        // 4. Map sang DTO (KHÔNG trả về isCorrect)
        List<QuestionResponseDTO> questionDTOs =
                questions.stream().map(this::mapToQuestionDTO).collect(Collectors.toList());

        int remainingPlays = maxPlaysPerDay - dailyPlay.getPlayCount();

        log.info("User {} bắt đầu game. Lượt hôm nay: {}/{}", userId, dailyPlay.getPlayCount(), maxPlaysPerDay);

        return StartGameResponseDTO.builder()
                .userId(userId)
                .remainingPlays(remainingPlays - 1) // -1 vì sắp dùng 1 lượt
                .totalPlaysToday(dailyPlay.getPlayCount())
                .questions(questionDTOs)
                .build();
    }

    /**
     * Nộp kết quả: chấm điểm, lưu session, cộng điểm, tăng lượt chơi
     */
    @Transactional
    public GameResultResponseDTO submitGame(SubmitAnswerRequestDTO request) {
        Long userId = request.getUserId();
        List<UserAnswerDTO> userAnswers = request.getAnswers();
        LocalDate today = LocalDate.now();

        // 1. Kiểm tra lại lượt chơi (double-check để tránh race condition)
        UserDailyPlay dailyPlay = userDailyPlayRepository
                .findByUserIdAndPlayDate(userId, today)
                .orElseThrow(() -> new AppException(ErrorCode.DAILY_PLAY_LIMIT_EXCEEDED));

        if (dailyPlay.getPlayCount() >= maxPlaysPerDay) {
            throw new AppException(ErrorCode.DAILY_PLAY_LIMIT_EXCEEDED);
        }

        // 2. Chấm điểm
        List<AnswerResultDTO> answerResults = new ArrayList<>();
        int score = 0;

        for (UserAnswerDTO userAnswer : userAnswers) {
            // Lấy đáp án đúng cho câu hỏi này
            Answer correctAnswer = answerRepository
                    .findByQuestionIdAndIsCorrectTrue(userAnswer.getQuestionId())
                    .orElse(null);

            if (correctAnswer == null) continue;

            boolean isCorrect = correctAnswer.getId().equals(userAnswer.getAnswerId());
            if (isCorrect) score++;

            // Lấy nội dung câu hỏi để hiển thị kết quả
            Question question = correctAnswer.getQuestion();

            answerResults.add(AnswerResultDTO.builder()
                    .questionId(userAnswer.getQuestionId())
                    .questionContent(question != null ? question.getContent() : "")
                    .selectedAnswerId(userAnswer.getAnswerId())
                    .correctAnswerId(correctAnswer.getId())
                    .isCorrect(isCorrect)
                    .build());
        }

        // 3. Lưu game session
        GameSession session = GameSession.builder().userId(userId).score(score).build();
        session = gameSessionRepository.save(session);

        // 4. Cộng điểm cho user (tạo mới nếu chưa có)
        UserPoint userPoint = userPointRepository.findByUserId(userId).orElseGet(() -> {
            UserPoint newPoint =
                    UserPoint.builder().userId(userId).totalPoints(0).build();
            return userPointRepository.save(newPoint);
        });

        // Cộng điểm bằng @Modifying query để đảm bảo atomic
        userPointRepository.addPoints(userId, score);
        int updatedTotalPoints = userPoint.getTotalPoints() + score;

        // 5. Tăng lượt chơi trong ngày
        userDailyPlayRepository.incrementPlayCount(userId, today);

        log.info(
                "User {} hoàn thành game. Điểm: {}/{}. Tổng điểm: {}",
                userId,
                score,
                questionsPerGame,
                updatedTotalPoints);

        return GameResultResponseDTO.builder()
                .sessionId(session.getId())
                .userId(userId)
                .score(score)
                .totalPoints(updatedTotalPoints)
                .correctCount(score)
                .totalQuestions(userAnswers.size())
                .answerResults(answerResults)
                .build();
    }

    /**
     * Lấy thông tin lượt chơi hôm nay của user
     */
    public Map<String, Object> getDailyPlayInfo(Long userId) {
        LocalDate today = LocalDate.now();
        UserDailyPlay dailyPlay = userDailyPlayRepository
                .findByUserIdAndPlayDate(userId, today)
                .orElse(UserDailyPlay.builder()
                        .userId(userId)
                        .playDate(today)
                        .playCount(0)
                        .build());

        Map<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("playDate", today);
        info.put("playCount", dailyPlay.getPlayCount());
        info.put("maxPlays", maxPlaysPerDay);
        info.put("remainingPlays", Math.max(0, maxPlaysPerDay - dailyPlay.getPlayCount()));
        info.put("canPlay", dailyPlay.getPlayCount() < maxPlaysPerDay);
        return info;
    }

    /**
     * Lấy tổng điểm của user
     */
    public Map<String, Object> getUserPoints(Long userId) {
        UserPoint userPoint = userPointRepository
                .findByUserId(userId)
                .orElse(UserPoint.builder().userId(userId).totalPoints(0).build());

        Map<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("totalPoints", userPoint.getTotalPoints());
        return info;
    }

    // ===== PRIVATE HELPERS =====

    private QuestionResponseDTO mapToQuestionDTO(Question question) {
        List<AnswerResponseDTO> answerDTOs = question.getAnswers().stream()
                .map(answer -> AnswerResponseDTO.builder()
                        .id(answer.getId())
                        .content(answer.getContent())
                        // isCorrect bị ẩn để tránh gian lận
                        .build())
                .collect(Collectors.toList());

        // Shuffle đáp án để không luôn đáp án đúng ở vị trí cố định
        Collections.shuffle(answerDTOs);

        return QuestionResponseDTO.builder()
                .id(question.getId())
                .content(question.getContent())
                .topic(question.getTopic())
                .answers(answerDTOs)
                .build();
    }
}
