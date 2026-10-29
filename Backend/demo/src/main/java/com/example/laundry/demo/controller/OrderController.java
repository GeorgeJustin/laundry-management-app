
package com.example.laundry.demo.controller;

import com.example.laundry.demo.dto.AvailableSlotResponse;
import com.example.laundry.demo.dto.ConfirmOrderRequest;
import com.example.laundry.demo.dto.CreateOrderRequest;
import com.example.laundry.demo.dto.OrderResponse;
import com.example.laundry.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam String type) {

        return ResponseEntity.ok(
                orderService.getAvailableSlots(date, type)
        );
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @PathVariable String orderNumber) {

        return ResponseEntity.ok(
                orderService.getOrderByNumber(orderNumber)
        );
    }

    @PutMapping("/{orderNumber}/confirm-quantities")
    public ResponseEntity<OrderResponse> confirmOrderQuantities(
            @PathVariable String orderNumber,
            @Valid @RequestBody ConfirmOrderRequest request) {

        OrderResponse response =
                orderService.confirmOrderQuantities(orderNumber, request);

        return ResponseEntity.ok(response);
    }
}
