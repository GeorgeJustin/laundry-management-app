
package com.example.laundry.demo.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long serviceId,
        String serviceName,
        String pricingUnit,
        BigDecimal unitPrice,
        BigDecimal quantity,
        BigDecimal confirmedQuantity,
        BigDecimal estimatedLineTotal,
        BigDecimal actualWeightKg,
        BigDecimal finalLineTotal
) {}
