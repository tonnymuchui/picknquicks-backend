package com.picknquicks.event;
import com.picknquicks.domain.product.Product;
import lombok.Getter;

@Getter
public class ProductCreatedEvent extends ProductEvent {

    private final Product product;

    public ProductCreatedEvent(Object source, Product product) {
        super(source, product.getId(), "PRODUCT_CREATED");
        this.product = product;
    }

}