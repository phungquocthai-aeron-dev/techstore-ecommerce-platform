package com.techstore.quizgame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.quizgame.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionId(Long questionId);

    Optional<Answer> findByQuestionIdAndIsCorrectTrue(Long questionId);

    @Modifying
    @Query("DELETE FROM Answer a WHERE a.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
}
