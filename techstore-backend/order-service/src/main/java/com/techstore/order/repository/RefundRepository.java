package com.techstore.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techstore.order.entity.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {}
