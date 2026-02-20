package com.techstore.order.service.shipping.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.techstore.order.dto.response.GHNDistrictResponse.DistrictData;
import com.techstore.order.dto.response.GHNProvinceResponse.ProvinceData;
import com.techstore.order.dto.response.GHNWardResponse.WardData;
import com.techstore.order.dto.response.ShippingInfo;
import com.techstore.order.entity.Address;
import com.techstore.order.entity.Order;
import com.techstore.order.service.shipping.ShippingService;

@Service("GHTK")
public class GHTKShippingService implements ShippingService {

    @Override
    public String getType() {
        return "GHTK";
    }

    @Override
    public Double calculateFee(Long addressId, Double weight) {
        return 35000.0;
    }

    @Override
    public List<ProvinceData> getProvinces() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DistrictData> getDistricts(Long provinceId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WardData> getWards(Long districtId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ShippingInfo createShippingOrder(Order order, Address toAddress) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String generatePrintUrl(Order order) {
        // TODO Auto-generated method stub
        return null;
    }
}
