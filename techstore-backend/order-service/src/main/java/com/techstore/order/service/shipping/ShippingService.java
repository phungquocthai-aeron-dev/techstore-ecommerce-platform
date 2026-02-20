package com.techstore.order.service.shipping;

import java.util.List;

import com.techstore.order.dto.response.GHNDistrictResponse;
import com.techstore.order.dto.response.GHNProvinceResponse;
import com.techstore.order.dto.response.GHNWardResponse;
import com.techstore.order.dto.response.ShippingInfo;
import com.techstore.order.entity.Address;
import com.techstore.order.entity.Order;

public interface ShippingService {

    String getType();

    List<GHNProvinceResponse.ProvinceData> getProvinces();

    List<GHNDistrictResponse.DistrictData> getDistricts(Long provinceId);

    List<GHNWardResponse.WardData> getWards(Long districtId);

    Double calculateFee(Long toAddressId, Double weight);

    ShippingInfo createShippingOrder(Order order, Address toAddress);

    String generatePrintUrl(Order order);
}
