package com.techstore.order.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GHNProvinceResponse {

    private Integer code;
    private String message;
    private List<ProvinceData> data;

    @Getter
    @Setter
    public static class ProvinceData {

        @JsonProperty("ProvinceID")
        private Long provinceId;

        @JsonProperty("ProvinceName")
        private String provinceName;

        @JsonProperty("Code")
        private String code;

        @JsonProperty("Status")
        private Integer status;
    }
}
