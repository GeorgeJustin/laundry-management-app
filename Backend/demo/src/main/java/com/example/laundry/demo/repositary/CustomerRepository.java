package com.example.laundry.demo.repositary;

import com.example.laundry.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    List<Customer> findAllByOrderByIdAsc();

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(
            String phoneNumber, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email, Long id);
}
