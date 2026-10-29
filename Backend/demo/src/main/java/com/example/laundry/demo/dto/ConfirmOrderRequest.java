
package com.example.laundry.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ConfirmOrderRequest(

        @NotEmpty(message = "At least one order item is required")
        List<@Valid ConfirmOrderItemRequest> items

) {}
