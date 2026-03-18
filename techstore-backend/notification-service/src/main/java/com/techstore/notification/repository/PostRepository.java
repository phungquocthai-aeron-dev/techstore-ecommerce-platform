package com.techstore.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.techstore.notification.entity.Post;

public interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findAllByUserId(String userId, Pageable pageable);

    List<Post> findAllByUserIdAndIsReadFalse(String userId);

    @Query("{ 'userId': { $in: [?0, '0'] } }")
    Page<Post> findAllByUserIdOrGlobal(String userId, Pageable pageable);
}
