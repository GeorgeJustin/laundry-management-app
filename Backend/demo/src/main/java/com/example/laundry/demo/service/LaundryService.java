
package com.example.laundry.demo.service;

import com.example.laundry.demo.dto.ServicePageResponse;
import com.example.laundry.demo.dto.ServiceRequest;
import com.example.laundry.demo.dto.ServiceResponse;
import com.example.laundry.demo.entity.LaundryServiceEntity;
import com.example.laundry.demo.exception.ServiceNotFoundException;
import com.example.laundry.demo.repositary.ServiceRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LaundryService {

    private final ServiceRepository repository;

    public LaundryService(ServiceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        return toResponse(findService(id));
    }

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        LaundryServiceEntity entity = new LaundryServiceEntity(
                request.name().trim(),
                request.description(),
                request.price()
        );

        return toResponse(repository.save(entity));
    }

    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        LaundryServiceEntity entity = findService(id);

        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setPrice(request.price());

        return toResponse(repository.save(entity));
    }

    @Transactional
    public void deleteService(Long id) {
        repository.delete(findService(id));
    }

    // Keeps compatibility with callers that don't use search.
    @Transactional(readOnly = true)
    public ServicePageResponse getServicesPage(
            int page,
            int size,
            String sortBy,
            String direction) {

        return getServicesPage(page, size, sortBy, direction, null);
    }

    // Pagination, sorting and optional name search.
    @Transactional(readOnly = true)
    public ServicePageResponse getServicesPage(
            int page,
            int size,
            String sortBy,
            String direction,
            String search) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative.");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100.");
        }

        if (sortBy == null
                || !List.of("id", "name", "price").contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field. Use id, name or price.");
        }

        if (direction == null
                || !(direction.equalsIgnoreCase("asc")
                || direction.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException(
                    "Invalid sort direction. Use asc or desc.");
        }

        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<LaundryServiceEntity> servicePage;

        if (search == null || search.isBlank()) {
            servicePage = repository.findAll(pageable);
        } else {
            servicePage = repository.findByNameContainingIgnoreCase(
                    search.trim(), pageable);
        }

        List<ServiceResponse> content = servicePage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new ServicePageResponse(
                content,
                servicePage.getNumber(),
                servicePage.getSize(),
                servicePage.getTotalElements(),
                servicePage.getTotalPages(),
                servicePage.isFirst(),
                servicePage.isLast()
        );
    }

    private LaundryServiceEntity findService(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));
    }

    private ServiceResponse toResponse(LaundryServiceEntity entity) {
        return new ServiceResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice()
        );
    }
}
