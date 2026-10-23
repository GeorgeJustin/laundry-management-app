package com.example.laundry.demo.service;



import com.example.laundry.demo.dto.CustomerRequest;
import com.example.laundry.demo.dto.CustomerResponse;
import com.example.laundry.demo.entity.Customer;
import com.example.laundry.demo.repositary.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return toResponse(findCustomer(id));
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        String phone = normalizePhone(request.phoneNumber());
        String email = normalizeEmail(request.email());

        if (customerRepository.findByPhoneNumber(phone).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone number is already registered"
            );
        }

        if (email != null &&
                customerRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        Customer customer = new Customer();
        customer.setFullName(request.fullName().trim());
        customer.setPhoneNumber(phone);
        customer.setEmail(email);
        customer.setActive(true);

        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse updateCustomer(
            Long id, CustomerRequest request) {

        Customer customer = findCustomer(id);

        String phone = normalizePhone(request.phoneNumber());
        String email = normalizeEmail(request.email());

        if (customerRepository.existsByPhoneNumberAndIdNot(phone, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone number is already registered"
            );
        }

        if (email != null &&
                customerRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        customer.setFullName(request.fullName().trim());
        customer.setPhoneNumber(phone);
        customer.setEmail(email);

        return toResponse(customerRepository.save(customer));
    }

    public void deactivateCustomer(Long id) {
        Customer customer = findCustomer(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found: " + id
                ));
    }

    private String normalizePhone(String phone) {
        return phone.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
