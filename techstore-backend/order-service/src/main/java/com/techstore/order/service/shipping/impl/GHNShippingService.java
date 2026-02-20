package com.techstore.order.service.shipping.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.techstore.order.client.GHNClient;
import com.techstore.order.client.UserServiceClient;
import com.techstore.order.configuration.GHNConfig;
import com.techstore.order.configuration.WarehouseConfig;
import com.techstore.order.dto.response.CustomerResponse;
import com.techstore.order.dto.response.GHNDistrictResponse.DistrictData;
import com.techstore.order.dto.response.GHNProvinceResponse.ProvinceData;
import com.techstore.order.dto.response.GHNWardResponse.WardData;
import com.techstore.order.dto.response.ShippingInfo;
import com.techstore.order.entity.Address;
import com.techstore.order.entity.Order;
import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;
import com.techstore.order.repository.AddressRepository;
import com.techstore.order.service.shipping.ShippingService;

import lombok.RequiredArgsConstructor;

@Service("GHN")
@RequiredArgsConstructor
public class GHNShippingService implements ShippingService {

    private final AddressRepository addressRepository;
    private final GHNClient ghnClient;
    private final UserServiceClient userServiceClient;
    private final WarehouseConfig warehouseConfig;
    private final GHNConfig ghnConfig;

    @Override
    public String getType() {
        return "GHN";
    }

    @Override
    public List<ProvinceData> getProvinces() {
        return ghnClient.getProvinces();
    }

    @Override
    public List<DistrictData> getDistricts(Long provinceId) {
        return ghnClient.getDistricts(provinceId);
    }

    @Override
    public List<WardData> getWards(Long districtId) {
        return ghnClient.getWards(districtId);
    }

    @Override
    public Double calculateFee(Long toAddressId, Double weight) {

        Address toAddress = addressRepository
                .findById(toAddressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        Address fromAddress = new Address();
        fromAddress.setProvinceId(warehouseConfig.getProvinceId());
        fromAddress.setDistrictId(warehouseConfig.getDistrictId());
        fromAddress.setWardCode(warehouseConfig.getWardCode());

        return ghnClient.calculateFee(fromAddress, toAddress, weight);
    }

    @Override
    public ShippingInfo createShippingOrder(Order order, Address toAddress) {
        try {
            CustomerResponse customer =
                    userServiceClient.getCustomerById(toAddress.getCustomerId()).getResult();
            return ghnClient.createShippingOrder(order, toAddress, customer);

        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    @Override
    public String generatePrintUrl(Order order) {

        String token = ghnClient.generatePrintToken(order.getShippingCode());

        return ghnConfig.getBaseUrl().replace("/shiip/public-api", "") + "/a5/public-api/printA5?token=" + token;
    }
}
