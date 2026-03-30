package com.techstore.quizgame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.quizgame.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionId(Long questionId);

    Optional<Answer> findByQuestionIdAndIsCorrectTrue(Long questionId);
}
