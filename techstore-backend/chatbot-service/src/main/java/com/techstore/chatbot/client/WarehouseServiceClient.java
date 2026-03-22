package com.techstore.chatbot.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.techstore.chatbot.configuration.FileFeignConfig;
import com.techstore.chatbot.dto.response.ApiResponse;
import com.techstore.chatbot.dto.response.VariantStockResponse;

@FeignClient(name = "warehouse-service", url = "${app.services.warehouse}", configuration = FileFeignConfig.class)
public interface WarehouseServiceClient {

    @GetMapping("/inventory/variant/{variantId}/total-stock")
    ApiResponse<Long> getTotalStockByVariant(@PathVariable Long variantId);

    @PostMapping("/inventory/variant/total-stock/batch")
    ApiResponse<List<VariantStockResponse>> getTotalStockByVariants(@RequestBody List<Long> variantIds);
}
