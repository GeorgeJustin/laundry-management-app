

package com.example.laundry.demo.controller;

import com.example.laundry.demo.dto.ServicePageResponse;
import com.example.laundry.demo.dto.ServiceRequest;
import com.example.laundry.demo.dto.ServiceResponse;
import com.example.laundry.demo.service.LaundryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaundryService laundryService;

    @Test
    void getAllServices_shouldReturnOk() throws Exception {
        ServiceResponse response = new ServiceResponse(
                1L, "Wash and Fold",
                "Wash, dry and fold clothes",
                new BigDecimal("100.00"));

        when(laundryService.getAllServices())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Wash and Fold"))
                .andExpect(jsonPath("$[0].price").value(100.00));

        verify(laundryService).getAllServices();
    }

    @Test
    void getServiceById_shouldReturnService() throws Exception {
        ServiceResponse response = new ServiceResponse(
                1L, "Wash and Fold",
                "Wash, dry and fold clothes",
                new BigDecimal("100.00"));

        when(laundryService.getServiceById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wash and Fold"))
                .andExpect(jsonPath("$.description")
                        .value("Wash, dry and fold clothes"))
                .andExpect(jsonPath("$.price").value(100.00));

        verify(laundryService).getServiceById(1L);
    }

    @Test
    void createService_shouldReturnCreated() throws Exception {
        ServiceResponse response = new ServiceResponse(
                1L, "Wash and Fold",
                "Wash, dry and fold clothes",
                new BigDecimal("100.00"));

        when(laundryService.createService(any(ServiceRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "name": "Wash and Fold",
                    "description": "Wash, dry and fold clothes",
                    "price": 100.00
                }
                """;

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wash and Fold"))
                .andExpect(jsonPath("$.price").value(100.00));

        verify(laundryService).createService(any(ServiceRequest.class));
    }

    @Test
    void updateService_shouldReturnUpdatedService() throws Exception {
        ServiceResponse response = new ServiceResponse(
                1L, "Premium Wash",
                "Premium washing service",
                new BigDecimal("150.00"));

        when(laundryService.updateService(
                eq(1L), any(ServiceRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "name": "Premium Wash",
                    "description": "Premium washing service",
                    "price": 150.00
                }
                """;

        mockMvc.perform(put("/api/services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Premium Wash"))
                .andExpect(jsonPath("$.price").value(150.00));

        verify(laundryService).updateService(
                eq(1L), any(ServiceRequest.class));
    }

    @Test
    void deleteService_shouldReturnNoContent() throws Exception {
        doNothing().when(laundryService).deleteService(1L);

        mockMvc.perform(delete("/api/services/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(laundryService).deleteService(1L);
    }

    @Test
    void createService_withBlankName_shouldReturnBadRequest()
            throws Exception {
        String invalidRequestJson = """
                {
                    "name": "",
                    "description": "Wash, dry and fold clothes",
                    "price": 100.00
                }
                """;

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getServicesPage_shouldReturnOk() throws Exception {
        ServicePageResponse response = new ServicePageResponse(
                List.of(new ServiceResponse(
                        1L, "Wash and Fold",
                        "Wash, dry and fold clothes",
                        new BigDecimal("100.00"))),
                0, 5, 1, 1, true, true);

        when(laundryService.getServicesPage(
                0, 5, "id", "asc", null))
                .thenReturn(response);

        mockMvc.perform(get("/api/services/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Wash and Fold"));

        verify(laundryService)
                .getServicesPage(0, 5, "id", "asc", null);
    }

    @Test
    void getServicesPage_withNegativePage_shouldReturnBadRequest()
            throws Exception {
        when(laundryService.getServicesPage(
                -1, 5, "id", "asc", null))
                .thenThrow(new IllegalArgumentException(
                        "Page number cannot be negative."));

        mockMvc.perform(get("/api/services/page")
                        .param("page", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Page number cannot be negative."));
    }

    @Test
    void getServicesPage_withInvalidSortField_shouldReturnBadRequest()
            throws Exception {
        when(laundryService.getServicesPage(
                0, 5, "unknownField", "asc", null))
                .thenThrow(new IllegalArgumentException(
                        "Invalid sort field. Use id, name or price."));

        mockMvc.perform(get("/api/services/page")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "unknownField")
                        .param("direction", "asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void getServicesPage_withSearch_shouldReturnMatchingServices()
            throws Exception {
        ServicePageResponse response = new ServicePageResponse(
                List.of(new ServiceResponse(
                        2L, "Premium Wash",
                        "Premium washing service",
                        new BigDecimal("150.00"))),
                0, 5, 1, 1, true, true);

        when(laundryService.getServicesPage(
                0, 5, "id", "asc", "wash"))
                .thenReturn(response);

        mockMvc.perform(get("/api/services/page")
                        .param("search", "wash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Premium Wash"));

        verify(laundryService)
                .getServicesPage(0, 5, "id", "asc", "wash");
    }
}

