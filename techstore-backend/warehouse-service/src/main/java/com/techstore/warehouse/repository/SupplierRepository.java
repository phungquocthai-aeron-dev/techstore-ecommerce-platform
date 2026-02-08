package com.techstore.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techstore.warehouse.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByPhone(String phone);

    List<Supplier> findByStatus(String status);

    List<Supplier> findByNameContainingIgnoreCase(String name);
}
