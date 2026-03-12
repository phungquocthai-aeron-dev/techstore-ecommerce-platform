package com.techstore.order.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.techstore.order.entity.PaymentMethod;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findByName(String name);

    Page<PaymentMethod> findByStatus(String status, Pageable pageable);

    @Query(
            """
			SELECT p FROM PaymentMethod p
			WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (:status IS NULL OR p.status = :status)
		""")
    Page<PaymentMethod> searchPaymentMethods(String keyword, String status, Pageable pageable);
}
