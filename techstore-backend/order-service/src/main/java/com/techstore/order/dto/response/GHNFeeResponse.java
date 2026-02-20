package com.techstore.order.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GHNFeeResponse {

    private Integer code;
    private String message;
    private FeeData data;

    @Getter
    @Setter
    public static class FeeData {
        private Integer total;
    }
}
