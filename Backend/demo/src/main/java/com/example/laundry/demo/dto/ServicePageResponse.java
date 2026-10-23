package com.example.laundry.demo.dto;


import java.util.List;

public record ServicePageResponse(
        List<ServiceResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
