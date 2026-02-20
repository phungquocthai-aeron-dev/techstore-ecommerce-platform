package com.techstore.order.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GHNDistrictResponse {

    private Integer code;
    private String message;
    private List<DistrictData> data;

    @Getter
    @Setter
    public static class DistrictData {

        @JsonProperty("DistrictID")
        private Long districtId;

        @JsonProperty("DistrictName")
        private String districtName;

        @JsonProperty("ProvinceID")
        private Long provinceId;

        @JsonProperty("Status")
        private Integer status;
    }
}
