
package com.example.laundry.demo.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailableSlotResponse(
        LocalDate date,
        String type,
        LocalTime startTime,
        LocalTime endTime,
        long bookedOrders,
        int remainingCapacity,
        boolean available
) {
}
