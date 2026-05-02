package com.techstore.cart.infrastructure.feign;

import com.techstore.cart.domain.exception.VariantNotAvailableException;
import com.techstore.cart.domain.model.VariantInfo;
import com.techstore.cart.domain.service.ProductCatalogPort;
import com.techstore.cart.infrastructure.feign.client.ProductFeignClient;
import com.techstore.cart.infrastructure.feign.dto.ApiResponse;
import com.techstore.cart.infrastructure.feign.dto.VariantInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ProductCatalogAdapter - Adapts ProductFeignClient to domain ProductCatalogPort.
 * Translates infrastructure DTOs → domain model.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCatalogAdapter implements ProductCatalogPort {

    private final ProductFeignClient productFeignClient;

    @Override
    public VariantInfo getVariantById(Long variantId) {
        try {
            ApiResponse<VariantInfoDto> response = productFeignClient.getVariantById(variantId);

            if (response == null || response.getResult() == null) {
                throw new VariantNotAvailableException(variantId);
            }

            return toVariantInfo(response.getResult());
        } catch (VariantNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling product service for variant {}: {}", variantId, e.getMessage());
            throw new com.techstore.cart.infrastructure.feign.exception.ProductServiceException(
                    "Product service unavailable", e);
        }
    }

    private VariantInfo toVariantInfo(VariantInfoDto dto) {
        return VariantInfo.builder()
                .id(dto.getId())
                .productId(dto.getProductId())
                .color(dto.getColor())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .status(dto.getStatus())
                .imageUrl(dto.getImageUrl())
                .build();
    }
}
