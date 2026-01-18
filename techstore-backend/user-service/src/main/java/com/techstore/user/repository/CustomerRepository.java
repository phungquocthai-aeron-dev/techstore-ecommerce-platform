package com.techstore.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.user.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findById(Long id);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    @Query(
            value =
                    """
				SELECT *
				FROM customers c
				WHERE (:id IS NOT NULL AND c.id = :id)
				OR (:email IS NOT NULL AND c.email = :email)
				OR (:phone IS NOT NULL AND c.phone = :phone)
				OR (:fullName IS NOT NULL AND c.full_name LIKE CONCAT('%', :fullName, '%'))
			""",
            nativeQuery = true)
    List<Customer> findByIdOrEmailOrPhone(
            @Param("id") Long id,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("fullName") String fullName);

    boolean existsByEmail(String email);

    Optional<Customer> findByProviderAndProviderId(String provider, String providerId);
}
