package com.techstore.identity.repository;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.techstore.identity.entity.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    @Transactional
    @Modifying
    @Query("DELETE FROM InvalidatedToken t WHERE t.expiredAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

    boolean existsByToken(String token);
}
