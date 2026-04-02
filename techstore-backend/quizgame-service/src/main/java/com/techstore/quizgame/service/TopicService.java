package com.techstore.quizgame.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.quizgame.dto.request.TopicRequestDTO;
import com.techstore.quizgame.dto.response.TopicResponseDTO;
import com.techstore.quizgame.entity.Topic;
import com.techstore.quizgame.exception.AppException;
import com.techstore.quizgame.exception.ErrorCode;
import com.techstore.quizgame.repository.TopicRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private final TopicRepository topicRepository;

    /** Lấy tất cả chủ đề */
    public List<TopicResponseDTO> getAllTopics() {
        return topicRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /** Lấy chi tiết 1 chủ đề */
    public TopicResponseDTO getTopicById(Long id) {
        Topic topic = findTopicOrThrow(id);
        return mapToDTO(topic);
    }

    /** Tạo chủ đề mới */
    @Transactional
    public TopicResponseDTO createTopic(TopicRequestDTO request) {
        if (topicRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.TOPIC_NAME_EXISTED);
        }

        Topic topic = Topic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        topic = topicRepository.save(topic);
        log.info("Tạo chủ đề mới: id={}, name={}", topic.getId(), topic.getName());
        return mapToDTO(topic);
    }

    /** Cập nhật chủ đề */
    @Transactional
    public TopicResponseDTO updateTopic(Long id, TopicRequestDTO request) {
        Topic topic = findTopicOrThrow(id);

        // Kiểm tra tên trùng với topic KHÁC
        topicRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new AppException(ErrorCode.TOPIC_NAME_EXISTED);
            }
        });

        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        topic = topicRepository.save(topic);

        log.info("Cập nhật chủ đề: id={}, name={}", topic.getId(), topic.getName());
        return mapToDTO(topic);
    }

    /** Xóa chủ đề (cascade xóa luôn questions + answers) */
    @Transactional
    public void deleteTopic(Long id) {
        Topic topic = findTopicOrThrow(id);
        topicRepository.delete(topic);
        log.info("Xóa chủ đề: id={}, name={}", id, topic.getName());
    }

    // ===== HELPERS =====

    private Topic findTopicOrThrow(Long id) {
        return topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    }

    private TopicResponseDTO mapToDTO(Topic topic) {
        long questionCount = topic.getQuestions() != null ? topic.getQuestions().size() : 0;
        return TopicResponseDTO.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .questionCount(questionCount)
                .build();
    }
}
