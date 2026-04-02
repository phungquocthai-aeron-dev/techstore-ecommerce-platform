package com.techstore.quizgame.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.quizgame.dto.request.AnswerRequestDTO;
import com.techstore.quizgame.dto.request.QuestionRequestDTO;
import com.techstore.quizgame.dto.response.AnswerDetailResponseDTO;
import com.techstore.quizgame.dto.response.QuestionDetailResponseDTO;
import com.techstore.quizgame.entity.Answer;
import com.techstore.quizgame.entity.Question;
import com.techstore.quizgame.entity.Topic;
import com.techstore.quizgame.exception.AppException;
import com.techstore.quizgame.exception.ErrorCode;
import com.techstore.quizgame.repository.AnswerRepository;
import com.techstore.quizgame.repository.QuestionRepository;
import com.techstore.quizgame.repository.TopicRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionAdminService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TopicRepository topicRepository;

    /** Lấy tất cả câu hỏi (admin — có isCorrect) */
    public List<QuestionDetailResponseDTO> getAllQuestions() {
        return questionRepository.findAll().stream().map(this::mapToDetailDTO).collect(Collectors.toList());
    }

    /** Lấy câu hỏi theo chủ đề */
    public List<QuestionDetailResponseDTO> getQuestionsByTopic(Long topicId) {
        findTopicOrThrow(topicId); // validate topic tồn tại
        return questionRepository.findByTopicId(topicId).stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    /** Lấy chi tiết 1 câu hỏi */
    public QuestionDetailResponseDTO getQuestionById(Long id) {
        return mapToDetailDTO(findQuestionOrThrow(id));
    }

    /** Tạo câu hỏi kèm đáp án */
    @Transactional
    public QuestionDetailResponseDTO createQuestion(QuestionRequestDTO request) {
        validateAnswers(request.getAnswers());

        Topic topic = findTopicOrThrow(request.getTopicId());

        Question question = Question.builder()
                .content(request.getContent())
                .topic(topic)
                .topicName(topic.getName()) // giữ tương thích
                .build();

        question = questionRepository.save(question);

        // Lưu answers
        final Question savedQuestion = question;
        List<Answer> answers = request.getAnswers().stream()
                .map(dto -> Answer.builder()
                        .content(dto.getContent())
                        .isCorrect(dto.getIsCorrect())
                        .question(savedQuestion)
                        .build())
                .collect(Collectors.toList());

        answerRepository.saveAll(answers);
        question.setAnswers(answers);

        log.info("Tạo câu hỏi mới: id={}, topic={}", question.getId(), topic.getName());
        return mapToDetailDTO(question);
    }

    /** Cập nhật câu hỏi (nội dung + chủ đề, không đụng answers) */
    @Transactional
    public QuestionDetailResponseDTO updateQuestion(Long id, QuestionRequestDTO request) {
        validateAnswers(request.getAnswers());

        Question question = findQuestionOrThrow(id);
        Topic topic = findTopicOrThrow(request.getTopicId());

        question.setContent(request.getContent());
        question.setTopic(topic);
        question.setTopicName(topic.getName());

        // Xóa answers cũ, thêm answers mới
        answerRepository.deleteByQuestionId(id);

        List<Answer> newAnswers = request.getAnswers().stream()
                .map(dto -> Answer.builder()
                        .content(dto.getContent())
                        .isCorrect(dto.getIsCorrect())
                        .question(question)
                        .build())
                .collect(Collectors.toList());

        answerRepository.saveAll(newAnswers);
        question.setAnswers(newAnswers);
        questionRepository.save(question);

        log.info("Cập nhật câu hỏi: id={}", id);
        return mapToDetailDTO(question);
    }

    /** Xóa câu hỏi (cascade xóa answers) */
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = findQuestionOrThrow(id);
        questionRepository.delete(question);
        log.info("Xóa câu hỏi: id={}", id);
    }

    // ===== ANSWER CRUD =====

    /** Thêm 1 đáp án vào câu hỏi có sẵn */
    @Transactional
    public AnswerDetailResponseDTO addAnswer(Long questionId, AnswerRequestDTO request) {
        Question question = findQuestionOrThrow(questionId);

        // Nếu isCorrect = true, kiểm tra câu hỏi đã có đáp án đúng chưa
        if (Boolean.TRUE.equals(request.getIsCorrect())) {
            boolean hasCorrect = answerRepository
                    .findByQuestionIdAndIsCorrectTrue(questionId)
                    .isPresent();
            if (hasCorrect) {
                throw new AppException(ErrorCode.QUESTION_MUST_HAVE_ONE_CORRECT_ANSWER);
            }
        }

        Answer answer = Answer.builder()
                .content(request.getContent())
                .isCorrect(request.getIsCorrect())
                .question(question)
                .build();

        answer = answerRepository.save(answer);
        log.info("Thêm đáp án {} vào câu hỏi {}", answer.getId(), questionId);
        return mapAnswerToDTO(answer);
    }

    /** Cập nhật 1 đáp án */
    @Transactional
    public AnswerDetailResponseDTO updateAnswer(Long questionId, Long answerId, AnswerRequestDTO request) {
        findQuestionOrThrow(questionId);
        Answer answer = findAnswerOrThrow(answerId);

        // Kiểm tra answer thuộc question
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new AppException(ErrorCode.ANSWER_NOT_BELONG_TO_QUESTION);
        }

        // Nếu đổi sang isCorrect = true, kiểm tra không có answer đúng khác
        if (Boolean.TRUE.equals(request.getIsCorrect()) && !Boolean.TRUE.equals(answer.getIsCorrect())) {
            answerRepository.findByQuestionIdAndIsCorrectTrue(questionId).ifPresent(existing -> {
                if (!existing.getId().equals(answerId)) {
                    throw new AppException(ErrorCode.QUESTION_MUST_HAVE_ONE_CORRECT_ANSWER);
                }
            });
        }

        answer.setContent(request.getContent());
        answer.setIsCorrect(request.getIsCorrect());
        answer = answerRepository.save(answer);

        log.info("Cập nhật đáp án: id={}", answerId);
        return mapAnswerToDTO(answer);
    }

    /** Xóa 1 đáp án */
    @Transactional
    public void deleteAnswer(Long questionId, Long answerId) {
        findQuestionOrThrow(questionId);
        Answer answer = findAnswerOrThrow(answerId);

        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new AppException(ErrorCode.ANSWER_NOT_BELONG_TO_QUESTION);
        }

        answerRepository.delete(answer);
        log.info("Xóa đáp án: id={}", answerId);
    }

    // ===== HELPERS =====

    private void validateAnswers(List<AnswerRequestDTO> answers) {
        long correctCount = answers.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .count();
        if (correctCount != 1) {
            throw new AppException(ErrorCode.QUESTION_MUST_HAVE_ONE_CORRECT_ANSWER);
        }
    }

    private Topic findTopicOrThrow(Long id) {
        return topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    }

    private Question findQuestionOrThrow(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
    }

    private Answer findAnswerOrThrow(Long id) {
        return answerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));
    }

    private QuestionDetailResponseDTO mapToDetailDTO(Question question) {
        List<AnswerDetailResponseDTO> answerDTOs = question.getAnswers() == null
                ? List.of()
                : question.getAnswers().stream().map(this::mapAnswerToDTO).collect(Collectors.toList());

        return QuestionDetailResponseDTO.builder()
                .id(question.getId())
                .content(question.getContent())
                .topicId(question.getTopic() != null ? question.getTopic().getId() : null)
                .topicName(question.getTopic() != null ? question.getTopic().getName() : question.getTopicName())
                .answers(answerDTOs)
                .build();
    }

    private AnswerDetailResponseDTO mapAnswerToDTO(Answer answer) {
        return AnswerDetailResponseDTO.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .isCorrect(answer.getIsCorrect())
                .build();
    }
}
