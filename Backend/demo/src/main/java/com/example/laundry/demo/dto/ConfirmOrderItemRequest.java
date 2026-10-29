
package com.example.laundry.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ConfirmOrderItemRequest(

        @NotNull(message = "Order item ID is required")
        @Positive(message = "Order item ID must be positive")
        Long itemId,

        @DecimalMin(
                value = "0.001",
                message = "Actual weight must be greater than zero"
        )
        BigDecimal actualWeightKg,

        @DecimalMin(
                value = "0.001",
                message = "Confirmed quantity must be greater than zero"
        )
        BigDecimal confirmedQuantity

) {}
