
package com.example.laundry.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "laundry_orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_laundry_order_number",
                        columnNames = "order_number"
                )
        }
)
public class LaundryOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    // Optional for now: supports admin-created/guest orders.
    // Customer login and ownership checks can be added later.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Contact/address snapshots keep order details available
    // even if the customer's profile changes later.
    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "pickup_address", nullable = false, length = 500)
    private String pickupAddress;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    @Column(name = "pickup_scheduled_at", nullable = false)
    private LocalDateTime pickupScheduledAt;

    @Column(name = "delivery_scheduled_at", nullable = false)
    private LocalDateTime deliveryScheduledAt;

    @Column(name = "estimated_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedTotal = BigDecimal.ZERO;

    // Filled in after staff confirms the actual weight/final amount.
    @Column(name = "final_total", precision = 10, scale = 2)
    private BigDecimal finalTotal;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "customer_notes", length = 1000)
    private String customerNotes;

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus = "PENDING";

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod = "CASH_ON_DELIVERY";

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus = "PENDING";

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<LaundryOrderItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LaundryOrderEntity() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addItem(LaundryOrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getPickupScheduledAt() {
        return pickupScheduledAt;
    }

    public LocalDateTime getDeliveryScheduledAt() {
        return deliveryScheduledAt;
    }

    public BigDecimal getEstimatedTotal() {
        return estimatedTotal;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public List<LaundryOrderItemEntity> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setCustomer(Customer     customer) {
        this.customer = customer;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public void setPickupScheduledAt(LocalDateTime pickupScheduledAt) {
        this.pickupScheduledAt = pickupScheduledAt;
    }

    public void setDeliveryScheduledAt(LocalDateTime deliveryScheduledAt) {
        this.deliveryScheduledAt = deliveryScheduledAt;
    }

    public void setEstimatedTotal(BigDecimal estimatedTotal) {
        this.estimatedTotal = estimatedTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setItems(List<LaundryOrderItemEntity> items) {
        this.items.clear();

        if (items != null) {
            items.forEach(this::addItem);
        }
    }
}
