package com.techstore.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.product.dto.request.BrandCreateRequestDTO;
import com.techstore.product.dto.request.BrandSearchRequestDTO;
import com.techstore.product.dto.request.BrandStatusUpdateRequestDTO;
import com.techstore.product.dto.request.BrandUpdateRequestDTO;
import com.techstore.product.dto.response.BrandResponseDTO;
import com.techstore.product.dto.response.PageResponseDTO;
import com.techstore.product.entity.Brand;
import com.techstore.product.exception.AppException;
import com.techstore.product.exception.ErrorCode;
import com.techstore.product.mapper.BrandMapper;
import com.techstore.product.repository.BrandRepository;

@Service
@Transactional
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private BrandMapper brandMapper;

    /**
     * Thêm brand mới (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponseDTO createBrand(BrandCreateRequestDTO requestDTO) {
        Brand brand = new Brand();
        brand.setName(requestDTO.getName());
        brand.setStatus(requestDTO.getStatus());

        Brand savedBrand = brandRepository.save(brand);
        return brandMapper.toResponseDTO(savedBrand);
    }

    /**
     * Cập nhật brand (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponseDTO updateBrand(Long id, BrandUpdateRequestDTO requestDTO) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        if (requestDTO.getName() != null) {
            brand.setName(requestDTO.getName());
        }
        if (requestDTO.getStatus() != null) {
            brand.setStatus(requestDTO.getStatus());
        }

        Brand updatedBrand = brandRepository.save(brand);
        return brandMapper.toResponseDTO(updatedBrand);
    }

    /**
     * Cập nhật status brand (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponseDTO updateBrandStatus(Long id, BrandStatusUpdateRequestDTO requestDTO) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        brand.setStatus(requestDTO.getStatus());
        Brand updatedBrand = brandRepository.save(brand);
        return brandMapper.toResponseDTO(updatedBrand);
    }

    /**
     * Xóa brand (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandRepository.delete(brand);
    }

    /**
     * Lấy brand theo ID
     */
    @Transactional(readOnly = true)
    public BrandResponseDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return brandMapper.toResponseDTO(brand);
    }

    /**
     * Lấy tất cả brands (phân trang)
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<BrandResponseDTO> getAllBrands(int page, int size, String sortBy, String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Brand> brandPage = brandRepository.findAll(pageable);

        List<BrandResponseDTO> content =
                brandPage.getContent().stream().map(brandMapper::toResponseDTO).toList();

        return new PageResponseDTO<>(
                content,
                brandPage.getNumber(),
                brandPage.getSize(),
                brandPage.getTotalElements(),
                brandPage.getTotalPages(),
                brandPage.isLast());
    }

    /**
     * Lấy brands theo status
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<BrandResponseDTO> getBrandsByStatus(
            String status, int page, int size, String sortBy, String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Brand> brandPage = brandRepository.findByStatus(status, pageable);

        List<BrandResponseDTO> content =
                brandPage.getContent().stream().map(brandMapper::toResponseDTO).toList();

        return new PageResponseDTO<>(
                content,
                brandPage.getNumber(),
                brandPage.getSize(),
                brandPage.getTotalElements(),
                brandPage.getTotalPages(),
                brandPage.isLast());
    }

    /**
     * Tìm kiếm brands
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<BrandResponseDTO> searchBrands(BrandSearchRequestDTO searchDTO) {
        Sort sort = searchDTO.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(searchDTO.getSortBy()).ascending()
                : Sort.by(searchDTO.getSortBy()).descending();

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        Page<Brand> brandPage = brandRepository.searchBrands(searchDTO.getKeyword(), searchDTO.getStatus(), pageable);

        List<BrandResponseDTO> content =
                brandPage.getContent().stream().map(brandMapper::toResponseDTO).toList();

        return new PageResponseDTO<>(
                content,
                brandPage.getNumber(),
                brandPage.getSize(),
                brandPage.getTotalElements(),
                brandPage.getTotalPages(),
                brandPage.isLast());
    }
}
