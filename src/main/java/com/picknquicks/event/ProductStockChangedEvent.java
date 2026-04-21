package com.picknquicks.event;

import java.util.UUID;

public class ProductStockChangedEvent extends ProductEvent {

    private final Integer oldStock;
    private final Integer newStock;

    public ProductStockChangedEvent(Object source, UUID productId, Integer oldStock, Integer newStock) {
        super(source, productId, "PRODUCT_STOCK_CHANGED");
        this.oldStock = oldStock;
        this.newStock = newStock;
    }

    public Integer getOldStock() {
        return oldStock;
    }

    public Integer getNewStock() {
        return newStock;
    }
}