package com.techstore.order.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.techstore.order.configuration.FileFeignConfig;
import com.techstore.order.dto.request.InventoryExportRequest;
import com.techstore.order.dto.response.ApiResponse;
import com.techstore.order.dto.response.WarehouseTransactionResponse;

@FeignClient(name = "warehouse-service", url = "${app.services.warehouse}", configuration = FileFeignConfig.class)
public interface WarehouseServiceClient {

    @PutMapping("/transactions/{id}/cancel")
    ApiResponse<WarehouseTransactionResponse> cancelTransaction(@PathVariable Long id);

    @PostMapping("/transactions/export")
    ApiResponse<List<Long>> exportInventory(@RequestBody InventoryExportRequest request);
}
