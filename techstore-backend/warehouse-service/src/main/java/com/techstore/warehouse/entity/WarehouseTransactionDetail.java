package com.techstore.warehouse.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouse_transaction_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransactionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quantity;
    private Long variantId;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private WarehouseTransaction transaction;

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;
}
