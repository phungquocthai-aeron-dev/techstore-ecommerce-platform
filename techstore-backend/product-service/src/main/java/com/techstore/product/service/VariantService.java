package com.techstore.product.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.techstore.product.client.FileServiceClient;
import com.techstore.product.client.WarehouseServiceClient;
import com.techstore.product.constant.UploadFolder;
import com.techstore.product.dto.request.VariantCreateRequestDTO;
import com.techstore.product.dto.request.VariantUpdateImageRequestDTO;
import com.techstore.product.dto.request.VariantUpdateRequestDTO;
import com.techstore.product.dto.response.ApiResponse;
import com.techstore.product.dto.response.FileResponse;
import com.techstore.product.dto.response.VariantResponseDTO;
import com.techstore.product.dto.response.VariantStockResponse;
import com.techstore.product.entity.Product;
import com.techstore.product.entity.Variant;
import com.techstore.product.exception.AppException;
import com.techstore.product.exception.ErrorCode;
import com.techstore.product.mapper.VariantMapper;
import com.techstore.product.repository.ProductRepository;
import com.techstore.product.repository.VariantRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
public class VariantService {

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VariantMapper variantMapper;

    @Autowired
    private FileServiceClient fileServiceClient;

    @Autowired
    private WarehouseServiceClient warehouseServiceClient;

    /**
     * Thêm variant cho product (ADMIN)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public VariantResponseDTO createVariant(Long productId, VariantCreateRequestDTO requestDTO) {

        Product product =
                productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Check trùng màu
        if (requestDTO.getColor() == null || requestDTO.getColor().isBlank()) {
            throw new AppException(ErrorCode.INVALID_COLOR);
        }

        if (variantRepository.existsByProductIdAndColor(productId, requestDTO.getColor())) {
            throw new AppException(ErrorCode.VARIANT_ALREADY_EXISTS);
        }

        if (requestDTO.getPrice() == null || requestDTO.getPrice() <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE);
        }

        if (requestDTO.getStatus() == null) {
            requestDTO.setStatus("ACTIVE");
        }

        Variant variant = variantMapper.toEntity(requestDTO);
        product.setStatus("ACTIVE");
        variant.setProduct(product);

        Variant savedVariant = variantRepository.save(variant);
        updateProductBasePrice(productId);
        return variantMapper.toResponseDTO(savedVariant);
    }

    /**
     * Cập nhật variant (ADMIN)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public VariantResponseDTO updateVariant(Long variantId, VariantUpdateRequestDTO requestDTO) {

        Variant variant =
                variantRepository.findById(variantId).orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        boolean recalcBasePrice = false;

        if (requestDTO.getColor() != null) {
            if (requestDTO.getColor().isBlank()) {
                throw new AppException(ErrorCode.INVALID_COLOR);
            }

            boolean exists = variantRepository.existsByProductIdAndColorAndIdNot(
                    variant.getProduct().getId(), requestDTO.getColor(), variant.getId());

            if (exists) {
                throw new AppException(ErrorCode.VARIANT_ALREADY_EXISTS);
            }

            variant.setColor(requestDTO.getColor());
        }

        if (requestDTO.getPrice() != null) {
            if (requestDTO.getPrice() <= 0) {
                throw new AppException(ErrorCode.INVALID_PRICE);
            }
            variant.setPrice(requestDTO.getPrice());
            recalcBasePrice = true;
        }

        if (requestDTO.getWeight() != null) {
            if (requestDTO.getPrice() <= 0) {
                throw new AppException(ErrorCode.INVALID_WEIGHT);
            }
            variant.setWeight(requestDTO.getWeight());
            recalcBasePrice = true;
        }

        if (requestDTO.getStatus() != null) {
            variant.setStatus(requestDTO.getStatus());
            recalcBasePrice = true;
        }

        Variant updatedVariant = variantRepository.save(variant);

        if (recalcBasePrice) {
            updateProductBasePrice(variant.getProduct().getId());
        }

        VariantResponseDTO dto = variantMapper.toResponseDTO(updatedVariant);
        return dto;
    }

    /**
     * Xóa variant (ADMIN)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteVariant(Long variantId) {
        Variant variant =
                variantRepository.findById(variantId).orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        variant.setStatus("INACTIVE");
        variantRepository.save(variant);
        updateProductBasePrice(variant.getProduct().getId());
    }

    /**
     * Lấy danh sách variant theo product
     */
    @Transactional(readOnly = true)
    public List<VariantResponseDTO> getVariantsByProductId(Long productId) {

        List<Variant> variants = variantRepository.findByProductId(productId);
        return variantMapper.toResponseDTOList(variants);
    }

    @Transactional(readOnly = true)
    public VariantResponseDTO getVariantById(Long variantId) {

        Variant variant =
                variantRepository.findById(variantId).orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        VariantResponseDTO dto = variantMapper.toResponseDTO(variant);
        return dto;
    }

    @Transactional(readOnly = true)
    public VariantResponseDTO getVariantWithStockById(Long variantId) {

        Variant variant =
                variantRepository.findById(variantId).orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        VariantResponseDTO dto = variantMapper.toResponseDTO(variant);

        try {
            Long stock =
                    warehouseServiceClient.getTotalStockByVariant(variantId).getResult();
            dto.setStock(stock);
        } catch (Exception e) {
            log.error("Failed to fetch stock for variantId={}", variantId, e);
            dto.setStock(null); // UNKNOWN
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<VariantResponseDTO> getVariantsWithStock(List<Long> variantIds) {

        // Lấy variant
        List<Variant> variants = variantRepository.findByIdIn(variantIds);

        // Gọi batch warehouse
        Map<Long, Long> stockMap = new HashMap<>();

        if (!variants.isEmpty()) {
            try {
                ApiResponse<List<VariantStockResponse>> response =
                        warehouseServiceClient.getTotalStockByVariants(variantIds);

                stockMap = response.getResult().stream()
                        .collect(Collectors.toMap(VariantStockResponse::getVariantId, VariantStockResponse::getStock));
            } catch (Exception e) {
                log.error("Batch stock fetch failed", e);
            }
        }

        // Map sang DTO
        Map<Long, Long> finalStockMap = stockMap;

        return variants.stream()
                .map(v -> {
                    VariantResponseDTO dto = variantMapper.toResponseDTO(v);
                    dto.setStock(finalStockMap.getOrDefault(v.getId(), null));
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VariantResponseDTO> getVariantByIds(List<Long> ids) {
        List<Variant> variants = variantRepository.findAllById(ids);
        return variantMapper.toResponseDTOList(variants);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public VariantResponseDTO updateVariantImage(Long variantId, MultipartFile file, VariantUpdateImageRequestDTO dto) {

        Variant variant =
                variantRepository.findById(variantId).orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        // 1️ Validate oldUrl (nếu gửi)
        if (dto != null && dto.getOldUrl() != null) {

            if (variant.getImageUrl() == null) {
                throw new AppException(ErrorCode.VARIANT_IMAGE_NOT_FOUND);
            }

            String current = normalizeImagePath(variant.getImageUrl());
            String old = normalizeImagePath(dto.getOldUrl());

            if (!current.equals(old)) {
                throw new AppException(ErrorCode.VARIANT_IMAGE_NOT_FOUND);
            }
        }

        // 2️ Upload file mới
        if (file != null && !file.isEmpty()) {

            FileResponse uploaded = fileServiceClient
                    .upload(file, UploadFolder.PRODUCT_IMAGE.name())
                    .getResult();

            variant.setImageUrl(uploaded.getUrl());
        }

        Variant saved = variantRepository.save(variant);

        VariantResponseDTO dtoRes = variantMapper.toResponseDTO(saved);
        return dtoRes;
    }

    private void updateProductBasePrice(Long productId) {

        Double minPrice = variantRepository.findMinActivePriceByProductId(productId);

        Product product =
                productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setBasePrice(minPrice != null ? minPrice : 0.0);

        productRepository.save(product);
    }

    private String normalizeImagePath(String url) {
        if (url == null) return null;
        return url.replaceAll("^https?://[^/]+", "");
    }
}
