package com.techstore.order.client;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.techstore.order.configuration.GHNConfig;
import com.techstore.order.dto.request.GHNCreateOrderRequest;
import com.techstore.order.dto.request.GHNFeeRequest;
import com.techstore.order.dto.request.GHNWardRequest;
import com.techstore.order.dto.response.CustomerResponse;
import com.techstore.order.dto.response.GHNDistrictRequest;
import com.techstore.order.dto.response.GHNDistrictResponse;
import com.techstore.order.dto.response.GHNFeeResponse;
import com.techstore.order.dto.response.GHNProvinceResponse;
import com.techstore.order.dto.response.GHNWardResponse;
import com.techstore.order.dto.response.ShippingInfo;
import com.techstore.order.entity.Address;
import com.techstore.order.entity.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GHNClient {

    private final GHNConfig ghnConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnConfig.getToken());
        return headers;
    }

    // LẤY TỈNH
    public List<GHNProvinceResponse.ProvinceData> getProvinces() {

        String url = ghnConfig.getBaseUrl() + "/master-data/province";

        HttpEntity<?> entity = new HttpEntity<>(buildHeaders());

        ResponseEntity<GHNProvinceResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, GHNProvinceResponse.class);

        return response.getBody() != null ? response.getBody().getData() : List.of();
    }

    // LẤY QUẬN / HUYỆN
    public List<GHNDistrictResponse.DistrictData> getDistricts(Long provinceId) {

        String url = ghnConfig.getBaseUrl() + "/master-data/district";

        GHNDistrictRequest request = new GHNDistrictRequest(provinceId);

        HttpEntity<GHNDistrictRequest> entity = new HttpEntity<>(request, buildHeaders());

        ResponseEntity<GHNDistrictResponse> response =
                restTemplate.postForEntity(url, entity, GHNDistrictResponse.class);

        return response.getBody() != null ? response.getBody().getData() : List.of();
    }

    // LẤY PHƯỜNG / XÃ
    public List<GHNWardResponse.WardData> getWards(Long districtId) {

        String url = ghnConfig.getBaseUrl() + "/master-data/ward";

        GHNWardRequest request = new GHNWardRequest(districtId);

        HttpEntity<GHNWardRequest> entity = new HttpEntity<>(request, buildHeaders());

        ResponseEntity<GHNWardResponse> response = restTemplate.postForEntity(url, entity, GHNWardResponse.class);

        return response.getBody() != null ? response.getBody().getData() : List.of();
    }

    // TÍNH PHÍ
    public Double calculateFee(Address from, Address to, Double weight) {

        String url = ghnConfig.getBaseUrl() + "/v2/shipping-order/fee";

        GHNFeeRequest request = GHNFeeRequest.builder()
                .serviceTypeId(2)
                .fromDistrictId(from.getDistrictId())
                .fromWardCode(from.getWardCode())
                .toDistrictId(to.getDistrictId())
                .toWardCode(to.getWardCode())
                .length(20)
                .width(15)
                .height(10)
                .weight(weight.intValue())
                .insuranceValue(0)
                .build();

        HttpEntity<GHNFeeRequest> entity = new HttpEntity<>(request, buildHeaders());

        ResponseEntity<GHNFeeResponse> response = restTemplate.postForEntity(url, entity, GHNFeeResponse.class);

        if (response.getBody() == null || response.getBody().getData() == null) {
            throw new RuntimeException("GHN calculate fee failed");
        }

        return response.getBody().getData().getTotal().doubleValue();
    }

    public ShippingInfo createShippingOrder(Order order, Address toAddress, CustomerResponse customer) {

        String url = ghnConfig.getBaseUrl() + "/v2/shipping-order/create";

        Address to = order.getAddress();

        // SET CỨNG địa chỉ gửi
        String fromName = "Techstore";
        String fromPhone = "0939110211";
        String fromAddress = "123 Đường 3 tháng 2, Xuân Khánh, Ninh Kiều, Cần Thơ";
        String fromWard = "Phường Xuân Khánh";
        String fromDistrict = "Quận Ninh Kiều";
        String fromProvince = "Cần Thơ";

        List<GHNCreateOrderRequest.Item> items = order.getOrderDetails().stream()
                .map(od -> GHNCreateOrderRequest.Item.builder()
                        .name(od.getName())
                        .code("SKU" + od.getVariantId())
                        .quantity(od.getQuantity())
                        .price(od.getPrice().intValue())
                        .length(20)
                        .weight(od.getTotalWeight().intValue())
                        .build())
                .toList();

        int totalWeight = order.getOrderDetails().stream()
                .mapToInt(od -> od.getTotalWeight().intValue())
                .sum();

        GHNCreateOrderRequest request = GHNCreateOrderRequest.builder()
                .payment_type_id(2)
                .note("Giao hàng nhanh")
                .required_note("KHONGCHOXEMHANG")
                .from_name(fromName)
                .from_phone(fromPhone)
                .from_address(fromAddress)
                .from_ward_name(fromWard)
                .from_district_name(fromDistrict)
                .from_province_name(fromProvince)
                .to_name(customer.getFullName())
                .to_phone(customer.getPhone())
                .to_address(to.getAddress())
                .to_ward_name(toAddress.getWardName())
                .to_district_name(toAddress.getDistrictName())
                .to_province_name(toAddress.getProvinceName())
                .cod_amount(order.getTotalPrice().intValue())
                .content("Đơn hàng #" + order.getId())
                .weight(totalWeight)
                .length(20)
                .width(15)
                .height(10)
                .service_type_id(2)
                .items(items)
                .build();

        HttpEntity<GHNCreateOrderRequest> entity = new HttpEntity<>(request, buildHeaders());

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getBody() == null || response.getBody().get("data") == null) {
            throw new RuntimeException("GHN create order failed");
        }

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

        ShippingInfo result = ShippingInfo.builder()
                .orderCode(data.get("order_code").toString())
                .shippingFee(((Number) data.get("total_fee")).doubleValue())
                .expectedDeliveryTime(
                        Instant.parse(data.get("expected_delivery_time").toString()))
                .build();
        return result;
    }

    public String generatePrintToken(String shippingCode) {

        String url = ghnConfig.getBaseUrl() + "/v2/a5/gen-token";

        Map<String, Object> body = Map.of("order_codes", List.of(shippingCode));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getBody() == null || response.getBody().get("data") == null) {
            throw new RuntimeException("GHN generate print token failed");
        }

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

        return data.get("token").toString();
    }
}
