package com.techstore.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.order.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerId(Long customerId);
}
