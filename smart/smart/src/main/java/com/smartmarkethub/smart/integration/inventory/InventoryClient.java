package com.smartmarkethub.smart.integration.inventory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "inventoryClient", url = "${inventory.service.url}")
public interface InventoryClient {

    @PostMapping("/api/stock")
    StockItemDto create(@RequestBody CreateStockItemRequest request);

    @GetMapping("/api/stock/{id}")
    StockItemDto get(@PathVariable("id") UUID id);

    @PutMapping("/api/stock/{id}/reduce")
    StockItemDto reduce(@PathVariable("id") UUID id, @RequestBody ReduceStockRequest request);
}


