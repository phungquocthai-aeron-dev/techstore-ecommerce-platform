package com.techstore.order.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GHNFeeRequest {

    @JsonProperty("service_type_id")
    private Integer serviceTypeId;

    @JsonProperty("from_district_id")
    private Long fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("to_district_id")
    private Long toDistrictId;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    private Integer length;
    private Integer width;
    private Integer height;
    private Integer weight;

    @JsonProperty("insurance_value")
    private Integer insuranceValue;
}
