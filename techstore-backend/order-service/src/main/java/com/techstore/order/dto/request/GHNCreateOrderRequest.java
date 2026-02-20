package com.techstore.order.dto.request;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GHNCreateOrderRequest {

    private Integer payment_type_id;
    private String note;
    private String required_note;

    private String from_name;
    private String from_phone;
    private String from_address;
    private String from_ward_name;
    private String from_district_name;
    private String from_province_name;

    private String to_name;
    private String to_phone;
    private String to_address;
    private String to_ward_name;
    private String to_district_name;
    private String to_province_name;

    private Integer cod_amount;
    private String content;

    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;

    private Integer service_type_id;

    private List<Item> items;

    @Getter
    @Setter
    @Builder
    public static class Item {
        private String name;
        private String code;
        private Integer quantity;
        private Integer price;
        private Integer length;
        private Integer weight;
    }
}
