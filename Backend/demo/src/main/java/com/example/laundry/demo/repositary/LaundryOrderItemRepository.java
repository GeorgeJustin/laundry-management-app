
package com.example.laundry.demo.repositary;

import com.example.laundry.demo.entity.LaundryOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaundryOrderItemRepository
        extends JpaRepository<LaundryOrderItemEntity, Long> {

    List<LaundryOrderItemEntity> findByOrderId(Long orderId);
}
