package com.techstore.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    private String address;
    private Long provinceId;
    private Long districtId;
    private String wardCode;
}
