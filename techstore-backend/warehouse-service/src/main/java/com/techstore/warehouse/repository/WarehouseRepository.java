package com.techstore.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techstore.warehouse.entity.Warehouse;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByName(String name);

    List<Warehouse> findByStatus(String status);

    List<Warehouse> findByAddressId(String addressId);
}
