package com.techstore.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.identity.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByEmail(String email);

    boolean existsByEmail(String email);
}
