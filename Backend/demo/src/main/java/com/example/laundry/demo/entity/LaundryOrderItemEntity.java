package com.example.laundry.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import jakarta.persistence.Column;

@Entity
@Table(name = "laundry_order_items")
public class LaundryOrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private LaundryOrderEntity order;

    // The original service can be referenced, but its details
    // are also copied below as an order-time snapshot.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private LaundryServiceEntity service;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "pricing_unit", nullable = false, length = 16)
    private String pricingUnit;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // For PIECE/ITEM, this is the count.
    // For KG services, it can be null until staff weighs the laundry.
    @Column(name = "quantity", precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(precision = 10, scale = 3)
    private BigDecimal confirmedQuantity;

    public BigDecimal getConfirmedQuantity() {
        return confirmedQuantity;
    }

    public void setConfirmedQuantity(BigDecimal confirmedQuantity) {
        this.confirmedQuantity = confirmedQuantity;
    }

    @Column(name = "actual_weight_kg", precision = 10, scale = 3)
    private BigDecimal actualWeightKg;

    @Column(name = "estimated_line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedLineTotal = BigDecimal.ZERO;

    @Column(name = "final_line_total", precision = 10, scale = 2)
    private BigDecimal finalLineTotal;

    public LaundryOrderItemEntity() {
    }

    public Long getId() {
        return id;
    }

    public LaundryOrderEntity getOrder() {
        return order;
    }

    public LaundryServiceEntity getService() {
        return service;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPricingUnit() {
        return pricingUnit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getActualWeightKg() {
        return actualWeightKg;
    }

    public BigDecimal getEstimatedLineTotal() {
        return estimatedLineTotal;
    }

    public BigDecimal getFinalLineTotal() {
        return finalLineTotal;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrder(LaundryOrderEntity order) {
        this.order = order;
    }

    public void setService(LaundryServiceEntity service) {
        this.service = service;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setPricingUnit(String pricingUnit) {
        this.pricingUnit = pricingUnit;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setActualWeightKg(BigDecimal actualWeightKg) {
        this.actualWeightKg = actualWeightKg;
    }

    public void setEstimatedLineTotal(BigDecimal estimatedLineTotal) {
        this.estimatedLineTotal = estimatedLineTotal;
    }

    public void setFinalLineTotal(BigDecimal finalLineTotal) {
        this.finalLineTotal = finalLineTotal;
    }
}

