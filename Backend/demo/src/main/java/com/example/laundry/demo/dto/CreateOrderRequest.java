
package com.example.laundry.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderRequest(

        // Optional until customer login is implemented.
        Long customerId,

        @NotBlank(message = "Customer name is required")
        @Size(max = 150)
        String customerName,

        @NotBlank(message = "Customer phone is required")
        @Size(max = 20)
        String customerPhone,

        @NotBlank(message = "Pickup address is required")
        @Size(max = 500)
        String pickupAddress,

        @NotBlank(message = "Delivery address is required")
        @Size(max = 500)
        String deliveryAddress,

        @NotNull(message = "Pickup schedule is required")
        @Future(message = "Pickup schedule must be in the future")
        LocalDateTime pickupScheduledAt,

        @NotNull(message = "Delivery schedule is required")
        @Future(message = "Delivery schedule must be in the future")
        LocalDateTime deliveryScheduledAt,

        @Size(max = 1000)
        String customerNotes,

        @NotEmpty(message = "Select at least one service")
        @Valid
        List<CreateOrderItemRequest> items
) {
}
