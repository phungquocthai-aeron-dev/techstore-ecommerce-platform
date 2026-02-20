package com.techstore.order.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressResponse {

    private Long id;
    private String address;
    private Long provinceId;
    private String provinceName;
    private Long districtId;
    private String districtName;
    private String wardCode;
    private String wardName;
    private Boolean status;
    private Long customerId;
}
