package com.techstore.quizgame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.quizgame.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByName(String name);

    boolean existsByName(String name);
}
