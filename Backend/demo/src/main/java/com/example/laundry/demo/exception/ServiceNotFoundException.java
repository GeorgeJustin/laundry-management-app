package com.example.laundry.demo.exception;



public class ServiceNotFoundException
        extends RuntimeException {

    public ServiceNotFoundException(Long id) {
        super("Laundry service not found with ID: " + id);
    }
}
