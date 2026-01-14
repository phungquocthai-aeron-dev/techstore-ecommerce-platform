package com.techstore.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.techstore.identity.entity.InvalidatedToken;

import jakarta.transaction.Transactional;

public interface InvalidatedTokenRepository
        extends JpaRepository<InvalidatedToken, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM InvalidatedToken t WHERE t.expiryTime < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
    
    boolean existsByToken(String token);
}
