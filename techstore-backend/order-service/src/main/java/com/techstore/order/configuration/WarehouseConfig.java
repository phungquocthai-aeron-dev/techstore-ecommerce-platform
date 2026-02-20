package com.techstore.order.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.ghn.shipping.warehouse")
@Getter
@Setter
public class WarehouseConfig {

    private Long provinceId;
    private Long districtId;
    private String wardCode;
}
