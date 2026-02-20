package com.techstore.order.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.techstore.order.client.GHNClient;
import com.techstore.order.dto.request.AddressRequest;
import com.techstore.order.dto.response.AddressResponse;
import com.techstore.order.entity.Address;
import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;
import com.techstore.order.mapper.AddressMapper;
import com.techstore.order.repository.AddressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepo;
    private final AddressMapper addressMapper;
    private final GHNClient ghnClient;

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public AddressResponse create(Long customerId, AddressRequest request) {

        //  Validate Province tồn tại
        var province = ghnClient.getProvinces().stream()
                .filter(p -> p.getProvinceId().equals(request.getProvinceId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PROVINCE));

        //  Validate District thuộc Province
        var district = ghnClient.getDistricts(request.getProvinceId()).stream()
                .filter(d -> d.getDistrictId().equals(request.getDistrictId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_DISTRICT));

        //  Validate Ward thuộc District
        var ward = ghnClient.getWards(request.getDistrictId()).stream()
                .filter(w -> w.getWardCode().equals(request.getWardCode()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_WARD));

        Address address = addressMapper.toEntity(request);

        address.setProvinceName(province.getProvinceName());
        address.setDistrictName(district.getDistrictName());
        address.setWardName(ward.getWardName());

        address.setCustomerId(customerId);
        address.setStatus(true);

        return addressMapper.toResponse(addressRepo.save(address));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public List<AddressResponse> getByCustomerId(Long customerId) {
        return addressRepo.findByCustomerId(customerId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public AddressResponse update(Long id, AddressRequest request) {

        Address address = addressRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        addressMapper.updateEntity(request, address);

        return addressMapper.toResponse(addressRepo.save(address));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public void delete(Long id) {
        Address address = addressRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        addressRepo.delete(address);
    }
}
