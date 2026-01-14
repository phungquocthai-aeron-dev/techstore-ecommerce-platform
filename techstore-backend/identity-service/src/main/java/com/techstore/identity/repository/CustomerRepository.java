package com.techstore.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.identity.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Customer> findByProviderAndProviderId(String provider, String providerId);
}
