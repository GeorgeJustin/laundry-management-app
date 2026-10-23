package com.example.laundry.demo.dto;



import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String fullName,
        String phoneNumber,
        String email,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
