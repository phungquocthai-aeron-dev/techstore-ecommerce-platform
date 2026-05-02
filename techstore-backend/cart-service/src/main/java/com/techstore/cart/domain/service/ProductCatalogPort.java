package com.techstore.cart.domain.service;

import com.techstore.cart.domain.model.VariantInfo;

/**
 * ProductCatalogPort - Domain port for querying product/variant information.
 * Implemented in infrastructure layer via Feign client.
 */
public interface ProductCatalogPort {
    VariantInfo getVariantById(Long variantId);
}
