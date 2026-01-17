package com.techstore.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.user.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Customer> findByProviderAndProviderId(String provider, String providerId);
}
