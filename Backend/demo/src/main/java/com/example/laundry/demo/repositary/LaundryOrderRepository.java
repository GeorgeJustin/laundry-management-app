
package com.example.laundry.demo.repositary;

import com.example.laundry.demo.entity.LaundryOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;


public interface LaundryOrderRepository
        extends JpaRepository<LaundryOrderEntity, Long> {

    Optional<LaundryOrderEntity> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    @Query("""
    SELECT COUNT(o)
    FROM LaundryOrderEntity o
    WHERE o.pickupScheduledAt >= :slotStart
      AND o.pickupScheduledAt < :slotEnd
      AND (o.orderStatus IS NULL
           OR UPPER(o.orderStatus) <> 'CANCELLED')
    """)
    long countActivePickupsInSlot(
            @Param("slotStart") LocalDateTime slotStart,
            @Param("slotEnd") LocalDateTime slotEnd
    );

    @Query("""
    SELECT COUNT(o)
    FROM LaundryOrderEntity o
    WHERE o.deliveryScheduledAt >= :slotStart
      AND o.deliveryScheduledAt < :slotEnd
      AND (o.orderStatus IS NULL
           OR UPPER(o.orderStatus) <> 'CANCELLED')
    """)
    long countActiveDeliveriesInSlot(
            @Param("slotStart") LocalDateTime slotStart,
            @Param("slotEnd") LocalDateTime slotEnd
    );

}

