
package com.example.laundry.demo.service;

import com.example.laundry.demo.dto.ServicePageResponse;
import com.example.laundry.demo.dto.ServiceRequest;
import com.example.laundry.demo.dto.ServiceResponse;
import com.example.laundry.demo.entity.LaundryServiceEntity;
import com.example.laundry.demo.exception.ServiceNotFoundException;
import com.example.laundry.demo.repositary.ServiceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaundryServiceTest {

    @Mock
    private ServiceRepository repository;

    @InjectMocks
    private LaundryService laundryService;

    private LaundryServiceEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = new LaundryServiceEntity(
                "Wash and Fold",
                "Wash, dry and fold clothes",
                new BigDecimal("100.00")
        );
        sampleEntity.setId(1L);
    }

    @Test
    void createService_shouldReturnSavedService() {
        ServiceRequest request = new ServiceRequest(
                "Wash and Fold",
                "Wash, dry and fold clothes",
                new BigDecimal("100.00")
        );

        when(repository.save(any(LaundryServiceEntity.class)))
                .thenAnswer(invocation -> {
                    LaundryServiceEntity entity =
                            invocation.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        ServiceResponse response =
                laundryService.createService(request);

        assertEquals(1L, response.id());
        assertEquals("Wash and Fold", response.name());
        assertEquals(new BigDecimal("100.00"), response.price());

        verify(repository).save(any(LaundryServiceEntity.class));
    }

    @Test
    void getAllServices_shouldReturnAllServices() {
        when(repository.findAll()).thenReturn(List.of(sampleEntity));

        List<ServiceResponse> responses =
                laundryService.getAllServices();

        assertEquals(1, responses.size());
        assertEquals("Wash and Fold", responses.get(0).name());

        verify(repository).findAll();
    }

    @Test
    void getServiceById_shouldReturnService() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(sampleEntity));

        ServiceResponse response =
                laundryService.getServiceById(1L);

        assertEquals(1L, response.id());
        assertEquals("Wash and Fold", response.name());

        verify(repository).findById(1L);
    }

    @Test
    void updateService_shouldUpdateExistingService() {
        ServiceRequest request = new ServiceRequest(
                "Premium Wash",
                "Premium washing service",
                new BigDecimal("150.00")
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(sampleEntity));
        when(repository.save(any(LaundryServiceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServiceResponse response =
                laundryService.updateService(1L, request);

        assertEquals("Premium Wash", response.name());
        assertEquals("Premium washing service", response.description());
        assertEquals(new BigDecimal("150.00"), response.price());

        verify(repository).findById(1L);
        verify(repository).save(sampleEntity);
    }

    @Test
    void deleteService_shouldDeleteExistingService() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(sampleEntity));

        laundryService.deleteService(1L);

        verify(repository).delete(sampleEntity);
    }

    @Test
    void getServiceById_shouldThrowWhenServiceDoesNotExist() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ServiceNotFoundException.class,
                () -> laundryService.getServiceById(999L)
        );

        verify(repository).findById(999L);
    }

    @Test
    void getServicesPage_shouldReturnCorrectPaginationMetadata() {
        Pageable pageable = PageRequest.of(
                0, 5, Sort.by(Sort.Direction.ASC, "id"));

        when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(
                        List.of(sampleEntity), pageable, 1));

        ServicePageResponse response =
                laundryService.getServicesPage(0, 5, "id", "asc");

        assertEquals(0, response.page());
        assertEquals(5, response.size());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
        assertEquals("Wash and Fold", response.content().get(0).name());

        verify(repository).findAll(pageable);
    }

    @Test
    void getServicesPage_shouldApplyDescendingPriceSort() {
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleEntity)));

        laundryService.getServicesPage(0, 5, "price", "desc");

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(repository).findAll(captor.capture());

        Pageable capturedPageable = captor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(5, capturedPageable.getPageSize());
        assertEquals(
                Sort.Direction.DESC,
                capturedPageable.getSort().getOrderFor("price").getDirection()
        );
    }

    @Test
    void getServicesPage_shouldRejectNegativePage() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> laundryService.getServicesPage(
                        -1, 5, "id", "asc")
        );

        assertEquals(
                "Page number cannot be negative.",
                exception.getMessage()
        );
    }

    @Test
    void getServicesPage_shouldRejectInvalidPageSize() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> laundryService.getServicesPage(
                        0, 0, "id", "asc")
        );

        assertEquals(
                "Page size must be between 1 and 100.",
                exception.getMessage()
        );
    }

    @Test
    void getServicesPage_shouldRejectInvalidSortField() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> laundryService.getServicesPage(
                        0, 5, "unknownField", "asc")
        );

        assertEquals(
                "Invalid sort field. Use id, name or price.",
                exception.getMessage()
        );
    }

    @Test
    void getServicesPage_shouldRejectInvalidSortDirection() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> laundryService.getServicesPage(
                        0, 5, "id", "sideways")
        );

        assertEquals(
                "Invalid sort direction. Use asc or desc.",
                exception.getMessage()
        );
    }

    @Test
    void getServicesPage_shouldSearchByName() {
        Pageable pageable = PageRequest.of(
                0, 5, Sort.by(Sort.Direction.ASC, "id"));

        when(repository.findByNameContainingIgnoreCase(
                eq("wash"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(sampleEntity), pageable, 1));

        ServicePageResponse response =
                laundryService.getServicesPage(
                        0, 5, "id", "asc", "wash");

        assertEquals(1, response.totalElements());
        assertEquals("Wash and Fold", response.content().get(0).name());

        verify(repository).findByNameContainingIgnoreCase(
                "wash", pageable);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getServicesPage_shouldReturnAllWhenSearchIsBlank() {
        Pageable pageable = PageRequest.of(
                0, 5, Sort.by(Sort.Direction.ASC, "id"));

        when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(
                        List.of(sampleEntity), pageable, 1));

        ServicePageResponse response =
                laundryService.getServicesPage(
                        0, 5, "id", "asc", "   ");

        assertEquals(1, response.totalElements());

        verify(repository).findAll(pageable);
        verify(repository, never())
                .findByNameContainingIgnoreCase(
                        any(String.class), any(Pageable.class));
    }

    @Test
    void getServicesPage_shouldTrimSearchText() {
        Pageable pageable = PageRequest.of(
                0, 5, Sort.by(Sort.Direction.ASC, "id"));

        when(repository.findByNameContainingIgnoreCase(
                eq("wash"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(sampleEntity), pageable, 1));

        laundryService.getServicesPage(
                0, 5, "id", "asc", "  wash  ");

        verify(repository).findByNameContainingIgnoreCase(
                eq("wash"), any(Pageable.class));
    }
}
