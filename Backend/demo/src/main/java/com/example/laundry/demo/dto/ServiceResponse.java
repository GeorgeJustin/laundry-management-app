package com.example.laundry.demo.dto;
import java.math.BigDecimal;

public record ServiceResponse(
        Long id,
        String name,
        String description,
        BigDecimal price
) {
}
