package com.techstore.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.order.entity.ShippingProvider;

public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, Long> {}
