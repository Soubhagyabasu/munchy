package com.muchy.order.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderHello {
    @GetMapping("/api/v1/orders")
    public String getOrders() {
        return "Hello from Order Service";
    }
}
