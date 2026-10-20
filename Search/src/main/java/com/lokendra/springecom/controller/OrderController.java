package com.lokendra.springecom.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lokendra.springecom.model.dto.OrderRequest;
import com.lokendra.springecom.model.dto.OrderResponse;
import com.lokendra.springecom.service.OrderService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders/place")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody OrderRequest orderRequest,
            org.springframework.security.core.Authentication authentication) {
        String username = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            username = authentication.getName();
        }
        OrderResponse orderResponse = orderService.placeOrder(orderRequest, username);
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            org.springframework.security.core.Authentication authentication) {

        String username = null;
        boolean isAdmin = false;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            username = authentication.getName();
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        List<OrderResponse> responses;
        if (username != null) {
            responses = orderService.getOrdersForUser(username, isAdmin);
        } else {
            responses = java.util.Collections.emptyList();
        }
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @org.springframework.web.bind.annotation.PathVariable String orderId,
            @RequestBody java.util.Map<String, String> body) {
        String newStatus = body.getOrDefault("status", "PLACED");
        OrderResponse response = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(response);
    }
}
