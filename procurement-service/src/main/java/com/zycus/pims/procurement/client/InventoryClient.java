package com.zycus.pims.procurement.client;

import com.zycus.pims.common.dto.GoodsReceiptSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);
    private final RestClient restClient;

    public InventoryClient(@Value("${app.inventory-service-url:http://localhost:8082}") String inventoryServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public boolean syncGoodsReceipt(GoodsReceiptSyncRequest request) {
        try {
            log.info("Synchronizing goods receipt {} with inventory service...", request.getReceiptNumber());
            restClient.post()
                    .uri("/api/inventory/stock/goods-receipt")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully synced goods receipt {} with inventory service", request.getReceiptNumber());
            return true;
        } catch (Exception e) {
            log.warn("Could not sync goods receipt {} directly with inventory service: {}", request.getReceiptNumber(), e.getMessage());
            return false;
        }
    }
}
