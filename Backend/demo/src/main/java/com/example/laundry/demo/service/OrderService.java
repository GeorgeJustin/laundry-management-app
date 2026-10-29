
package com.example.laundry.demo.service;

import com.example.laundry.demo.dto.ConfirmOrderItemRequest;
import com.example.laundry.demo.dto.ConfirmOrderRequest;
import com.example.laundry.demo.dto.CreateOrderItemRequest;
import com.example.laundry.demo.dto.CreateOrderRequest;
import com.example.laundry.demo.dto.OrderItemResponse;
import com.example.laundry.demo.dto.OrderResponse;
import com.example.laundry.demo.entity.Customer;
import com.example.laundry.demo.entity.LaundryOrderEntity;
import com.example.laundry.demo.entity.LaundryOrderItemEntity;
import com.example.laundry.demo.entity.LaundryServiceEntity;
import com.example.laundry.demo.repositary.CustomerRepository;
import com.example.laundry.demo.repositary.LaundryOrderRepository;
import com.example.laundry.demo.repositary.ServiceRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.example.laundry.demo.dto.AvailableSlotResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
@Service
public class OrderService {

    private final LaundryOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;

    @Value("${laundry.scheduling.max-orders-per-slot:5}")
    private int maxOrdersPerSlot;

    @Value("${laundry.scheduling.slot-duration-minutes:120}")
    private long slotDurationMinutes;

    @Value("${laundry.scheduling.pickup-slot-starts:10:00,12:00,16:00}")
    private String pickupSlotStarts;

    @Value("${laundry.scheduling.delivery-slot-starts:09:00,13:00,17:00}")
    private String deliverySlotStarts;

    public OrderService(
            LaundryOrderRepository orderRepository,
            CustomerRepository customerRepository,
            ServiceRepository serviceRepository) {

        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.serviceRepository = serviceRepository;
    }

    // CREATE ORDER
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        if (!request.deliveryScheduledAt()
                .isAfter(request.pickupScheduledAt())) {

            throw new IllegalArgumentException(
                    "Delivery must be scheduled after pickup."
            );
        }

        // Validate pickup and delivery slots before saving the order.
        validatePickupSlot(request.pickupScheduledAt());
        validateDeliverySlot(request.deliveryScheduledAt());

        Customer customer = null;

        if (request.customerId() != null) {

            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Customer not found: " + request.customerId()
                            )
                    );

            if (!customer.isActive()) {
                throw new IllegalArgumentException(
                        "Cannot create an order for an inactive customer."
                );
            }
        }

        LaundryOrderEntity order = new LaundryOrderEntity();

        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setCustomerName(request.customerName().trim());
        order.setCustomerPhone(request.customerPhone().trim());
        order.setPickupAddress(request.pickupAddress().trim());
        order.setDeliveryAddress(request.deliveryAddress().trim());
        order.setPickupScheduledAt(request.pickupScheduledAt());
        order.setDeliveryScheduledAt(request.deliveryScheduledAt());
        order.setCustomerNotes(request.customerNotes());

        order.setOrderStatus("PENDING");
        order.setPaymentMethod("CASH_ON_DELIVERY");
        order.setPaymentStatus("PENDING");
        order.setDeliveryFee(BigDecimal.ZERO);

        BigDecimal estimatedTotal = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.items()) {

            LaundryServiceEntity service =
                    serviceRepository.findById(itemRequest.serviceId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Service not found: " + itemRequest.serviceId()
                                    )
                            );

            BigDecimal quantity = itemRequest.quantity();

            BigDecimal lineTotal = service.getPrice()
                    .multiply(quantity)
                    .setScale(2, RoundingMode.HALF_UP);

            LaundryOrderItemEntity item =
                    new LaundryOrderItemEntity();

            item.setService(service);
            item.setServiceName(service.getName());
            item.setPricingUnit(service.getPricingUnit());
            item.setUnitPrice(service.getPrice());
            item.setQuantity(quantity);
            item.setEstimatedLineTotal(lineTotal);

            order.addItem(item);

            estimatedTotal = estimatedTotal.add(lineTotal);
        }

        order.setEstimatedTotal(
                estimatedTotal.add(order.getDeliveryFee())
        );

        LaundryOrderEntity savedOrder =
                orderRepository.save(order);

        return toResponse(savedOrder);
    }

    // GET ORDER BY ORDER NUMBER
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {

        LaundryOrderEntity order =
                orderRepository.findByOrderNumber(orderNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order not found: " + orderNumber
                                )
                        );

        return toResponse(order);
    }

    // STAFF CONFIRMS ACTUAL WEIGHT / QUANTITY AND FINAL BILL
    @Transactional
    public OrderResponse confirmOrderQuantities(
            String orderNumber,
            ConfirmOrderRequest request) {

        LaundryOrderEntity order =
                orderRepository.findByOrderNumber(orderNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order not found: " + orderNumber
                                )
                        );

        if ("CANCELLED".equalsIgnoreCase(order.getOrderStatus())
                || "DELIVERED".equalsIgnoreCase(order.getOrderStatus())) {

            throw new IllegalArgumentException(
                    "Cannot confirm quantities for a cancelled "
                            + "or delivered order."
            );
        }

        Set<Long> submittedIds = new HashSet<>();

        BigDecimal finalTotal = order.getDeliveryFee() == null
                ? BigDecimal.ZERO
                : order.getDeliveryFee();

        for (ConfirmOrderItemRequest input : request.items()) {

            if (!submittedIds.add(input.itemId())) {
                throw new IllegalArgumentException(
                        "Duplicate order item ID: " + input.itemId()
                );
            }

            LaundryOrderItemEntity item = order.getItems().stream()
                    .filter(existing ->
                            existing.getId().equals(input.itemId())
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Order item " + input.itemId()
                                            + " does not belong to order " + orderNumber
                            )
                    );

            String unit = item.getPricingUnit();

            if (unit == null) {
                throw new IllegalArgumentException(
                        "Pricing unit is missing for item " + item.getId()
                );
            }

            BigDecimal billableQuantity;

            if ("KG".equalsIgnoreCase(unit)) {

                if (input.actualWeightKg() == null
                        || input.actualWeightKg().signum() <= 0) {

                    throw new IllegalArgumentException(
                            "Actual weight is required and must be positive "
                                    + "for KG service item " + item.getId()
                    );
                }

                if (input.confirmedQuantity() != null) {
                    throw new IllegalArgumentException(
                            "Use actualWeightKg, not confirmedQuantity, "
                                    + "for KG service item " + item.getId()
                    );
                }

                item.setActualWeightKg(input.actualWeightKg());
                item.setConfirmedQuantity(null);

                billableQuantity = input.actualWeightKg();

            } else if ("PIECE".equalsIgnoreCase(unit)
                    || "ITEM".equalsIgnoreCase(unit)) {

                if (input.confirmedQuantity() == null
                        || input.confirmedQuantity().signum() <= 0) {

                    throw new IllegalArgumentException(
                            "Confirmed quantity is required and must be "
                                    + "positive for " + unit
                                    + " service item " + item.getId()
                    );
                }

                if (input.actualWeightKg() != null) {
                    throw new IllegalArgumentException(
                            "Use confirmedQuantity, not actualWeightKg, "
                                    + "for " + unit + " service item "
                                    + item.getId()
                    );
                }

                item.setConfirmedQuantity(input.confirmedQuantity());
                item.setActualWeightKg(null);

                billableQuantity = input.confirmedQuantity();

            } else {
                throw new IllegalArgumentException(
                        "Unsupported pricing unit: " + unit
                );
            }

            BigDecimal finalLineTotal = item.getUnitPrice()
                    .multiply(billableQuantity)
                    .setScale(2, RoundingMode.HALF_UP);

            item.setFinalLineTotal(finalLineTotal);

            finalTotal = finalTotal.add(finalLineTotal);
        }

        if (submittedIds.size() != order.getItems().size()) {
            throw new IllegalArgumentException(
                    "Submit confirmed quantities for every item in the order."
            );
        }

        order.setFinalTotal(finalTotal);
        order.setOrderStatus("RECEIVED_AND_INSPECTED");

        LaundryOrderEntity savedOrder =
                orderRepository.save(order);

        return toResponse(savedOrder);
    }

    // VALIDATE PICKUP SLOT AND CAPACITY
    private void validatePickupSlot(LocalDateTime scheduledAt) {

        LocalDateTime slotStart = validateConfiguredSlot(
                scheduledAt,
                pickupSlotStarts,
                "pickup"
        );

        LocalDateTime slotEnd =
                slotStart.plusMinutes(slotDurationMinutes);

        long bookedOrders =
                orderRepository.countActivePickupsInSlot(
                        slotStart,
                        slotEnd
                );

        if (bookedOrders >= maxOrdersPerSlot) {
            throw new IllegalArgumentException(
                    "This pickup slot is full. "
                            + "Please select another pickup time."
            );
        }
    }

    // VALIDATE DELIVERY SLOT AND CAPACITY
    private void validateDeliverySlot(LocalDateTime scheduledAt) {

        LocalDateTime slotStart = validateConfiguredSlot(
                scheduledAt,
                deliverySlotStarts,
                "delivery"
        );

        LocalDateTime slotEnd =
                slotStart.plusMinutes(slotDurationMinutes);

        long bookedOrders =
                orderRepository.countActiveDeliveriesInSlot(
                        slotStart,
                        slotEnd
                );

        if (bookedOrders >= maxOrdersPerSlot) {
            throw new IllegalArgumentException(
                    "This delivery slot is full. "
                            + "Please select another delivery time."
            );
        }
    }

    // VALIDATE CONFIGURED START TIME
    private LocalDateTime validateConfiguredSlot(
            LocalDateTime scheduledAt,
            String configuredStarts,
            String slotType) {

        if (scheduledAt == null
                || !scheduledAt.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "The " + slotType
                            + " schedule must be in the future."
            );
        }

        List<LocalTime> allowedStarts = Arrays.stream(
                        configuredStarts.split(",")
                )
                .map(String::trim)
                .map(LocalTime::parse)
                .toList();

        LocalTime requestedTime = scheduledAt.toLocalTime();

        if (!allowedStarts.contains(requestedTime)) {
            throw new IllegalArgumentException(
                    "Invalid " + slotType + " time. "
                            + "Available slot starts: " + configuredStarts
            );
        }

        return scheduledAt;
    }

    // GENERATE UNIQUE, READABLE ORDER NUMBER
    private String generateOrderNumber() {

        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String uniquePart = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        return "LND-" + date + "-" + uniquePart;
    }

    // CONVERT ENTITY TO RESPONSE DTO
    private OrderResponse toResponse(LaundryOrderEntity order) {

        List<OrderItemResponse> itemResponses =
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getService().getId(),
                                item.getServiceName(),
                                item.getPricingUnit(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getConfirmedQuantity(),
                                item.getEstimatedLineTotal(),
                                item.getActualWeightKg(),
                                item.getFinalLineTotal()
                        ))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getPickupAddress(),
                order.getDeliveryAddress(),
                order.getPickupScheduledAt(),
                order.getDeliveryScheduledAt(),
                order.getEstimatedTotal(),
                order.getFinalTotal(),
                order.getDeliveryFee(),
                order.getOrderStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    public List<AvailableSlotResponse> getAvailableSlots(
            LocalDate date, String type) {

        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Please select today or a future date.");
        }

        if (maxOrdersPerSlot <= 0 || slotDurationMinutes <= 0) {
            throw new IllegalStateException(
                    "Scheduling capacity and duration must be positive.");
        }

        if (type == null ||
                (!type.equalsIgnoreCase("PICKUP")
                        && !type.equalsIgnoreCase("DELIVERY"))) {
            throw new IllegalArgumentException(
                    "Type must be PICKUP or DELIVERY.");
        }

        String normalizedType = type.toUpperCase(java.util.Locale.ROOT);

        String configuredStarts = normalizedType.equals("PICKUP")
                ? pickupSlotStarts
                : deliverySlotStarts;

        LocalDateTime now = LocalDateTime.now();

        List<LocalTime> starts = Arrays.stream(configuredStarts.split(","))
                .map(String::trim)
                .map(LocalTime::parse)
                .distinct()
                .sorted()
                .toList();

        List<AvailableSlotResponse> slots = new ArrayList<>();

        for (LocalTime startTime : starts) {
            LocalDateTime slotStart = date.atTime(startTime);

            if (!slotStart.isAfter(now)) {
                continue;
            }

            LocalDateTime slotEnd =
                    slotStart.plusMinutes(slotDurationMinutes);

            long bookedOrders = normalizedType.equals("PICKUP")
                    ? orderRepository.countActivePickupsInSlot(
                    slotStart, slotEnd)
                    : orderRepository.countActiveDeliveriesInSlot(
                    slotStart, slotEnd);

            int remainingCapacity = (int) Math.max(
                    0, maxOrdersPerSlot - bookedOrders);

            slots.add(new AvailableSlotResponse(
                    date,
                    normalizedType,
                    startTime,
                    slotEnd.toLocalTime(),
                    bookedOrders,
                    remainingCapacity,
                    bookedOrders < maxOrdersPerSlot
            ));
        }

        return slots;
    }

}
