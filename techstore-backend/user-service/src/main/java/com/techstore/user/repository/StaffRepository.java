package com.techstore.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.user.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<Staff> findByEmail(String email);

    boolean existsByEmail(String email);
}
