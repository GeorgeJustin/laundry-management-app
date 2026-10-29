
package com.example.laundry.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String customerName,
        String customerPhone,
        String pickupAddress,
        String deliveryAddress,
        LocalDateTime pickupScheduledAt,
        LocalDateTime deliveryScheduledAt,
        BigDecimal estimatedTotal,
        BigDecimal finalTotal,
        BigDecimal deliveryFee,
        String orderStatus,
        String paymentMethod,
        String paymentStatus,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
