package com.example.laundry.demo.controller;



import com.example.laundry.demo.dto.ServiceRequest;
import com.example.laundry.demo.dto.ServiceResponse;
import com.example.laundry.demo.service.LaundryService;
import com.example.laundry.demo.dto.ServicePageResponse;
import org.springframework.web.bind.annotation.RequestParam;


import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final LaundryService laundryService;

    public ServiceController(LaundryService laundryService) {
        this.laundryService = laundryService;
    }
//
    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        return ResponseEntity.ok(
                laundryService.getAllServices()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                laundryService.getServiceById(id)
        );
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody ServiceRequest request) {

        ServiceResponse response =
                laundryService.createService(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/page")
    public ResponseEntity<ServicePageResponse> getServicesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search) {

        ServicePageResponse response =
                laundryService.getServicesPage(
                        page, size, sortBy, direction, search
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {

        return ResponseEntity.ok(
                laundryService.updateService(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long id) {

        laundryService.deleteService(id);

        return ResponseEntity.noContent().build();
    }
}
