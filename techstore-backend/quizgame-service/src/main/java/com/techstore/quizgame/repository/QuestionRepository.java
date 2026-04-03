package com.techstore.quizgame.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.quizgame.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM questions ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestions(Pageable pageable);

    @Query(value = "SELECT * FROM questions WHERE topic = :topic ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestionsByTopic(@Param("topic") String topic, Pageable pageable);

    long countByTopic_Name(String name);

    long countByTopic_Id(Long topicId);

    List<Question> findByTopicId(Long topicId);
}
