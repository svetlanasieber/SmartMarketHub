package com.smartmarkethub.smart.integration.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InventoryGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryGatewayService.class);

    private final InventoryClient inventoryClient;

    public InventoryGatewayService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    public StockItemDto checkAvailability(UUID stockItemId) {
        logger.debug("Checking availability for stockItemId={}", stockItemId);
        return inventoryClient.get(stockItemId);
    }

    public StockItemDto reserveStock(UUID stockItemId, int quantity) {
        logger.debug("Reserving quantity={} for stockItemId={}", quantity, stockItemId);
        ReduceStockRequest req = new ReduceStockRequest();
        req.setQuantity(quantity);
        return inventoryClient.reduce(stockItemId, req);
    }
}


