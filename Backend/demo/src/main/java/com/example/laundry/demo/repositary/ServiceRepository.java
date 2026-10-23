package com.example.laundry.demo.repositary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.laundry.demo.entity.LaundryServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository
        extends JpaRepository<LaundryServiceEntity, Long> {
    Page<LaundryServiceEntity> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}

