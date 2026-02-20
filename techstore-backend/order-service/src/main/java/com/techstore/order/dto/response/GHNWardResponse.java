package com.techstore.order.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GHNWardResponse {

    private Integer code;
    private String message;
    private List<WardData> data;

    @Getter
    @Setter
    public static class WardData {

        @JsonProperty("WardCode")
        private String wardCode;

        @JsonProperty("WardName")
        private String wardName;

        @JsonProperty("DistrictID")
        private Long districtId;

        @JsonProperty("Status")
        private Integer status;
    }
}
