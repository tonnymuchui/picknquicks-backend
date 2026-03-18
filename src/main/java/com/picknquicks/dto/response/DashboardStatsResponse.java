package com.picknquicks.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Admin dashboard statistics")
public class DashboardStatsResponse {

    @Schema(description = "Total number of users")
    private Long totalUsers;

    @Schema(description = "Total number of customers")
    private Long totalCustomers;

    @Schema(description = "Total number of active users")
    private Long activeUsers;

    @Schema(description = "Total number of orders")
    private Long totalOrders;

    @Schema(description = "Total revenue")
    private BigDecimal totalRevenue;

    @Schema(description = "Number of orders pending")
    private Long pendingOrders;

    @Schema(description = "Number of orders completed")
    private Long completedOrders;

    @Schema(description = "Total number of products")
    private Long totalProducts;

    @Schema(description = "Number of low stock products")
    private Long lowStockProducts;

    @Schema(description = "Number of out of stock products")
    private Long outOfStockProducts;
}