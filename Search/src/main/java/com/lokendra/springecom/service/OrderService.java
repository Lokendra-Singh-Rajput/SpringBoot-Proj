package com.lokendra.springecom.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lokendra.springecom.model.Order;
import com.lokendra.springecom.model.OrderItem;
import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.model.dto.OrderItemRequest;
import com.lokendra.springecom.model.dto.OrderItemResponse;
import com.lokendra.springecom.model.dto.OrderRequest;
import com.lokendra.springecom.model.dto.OrderResponse;
import com.lokendra.springecom.repo.OrderRepo;
import com.lokendra.springecom.repo.ProductRepo;

@Service
@Transactional
public class OrderService {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private OrderRepo orderRepo;

    public OrderResponse placeOrder(OrderRequest request) {
        return placeOrder(request, null);
    }

    public OrderResponse placeOrder(OrderRequest request, String username) {
        Order order = new Order();
        String orderId = "ORD" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        order.setOrderId(orderId);
        order.setCustomerName(request.customerName());
        order.setEmail(request.email());
        order.setUsername(username != null ? username : request.customerName());
        order.setStatus("PLACED");
        order.setOrderDate(LocalDate.now());
        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemRequest itemReq : request.items()){
            Product product = productRepo.findById(itemReq.productId())
                        .orElseThrow(()-> new RuntimeException("Product not found"));
            
            product.setStockQuantity(product.getStockQuantity()-itemReq.quantity());
            productRepo.save(product);

            OrderItem orderItem = OrderItem.builder()
                                    .product(product)
                                    .quantity(itemReq.quantity())
                                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())))
                                    .order(order)
                                    .build();
            orderItems.add(orderItem);

        }
        order.setOrderItems(orderItems);
        Order saveOrder = orderRepo.save(order);

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for(OrderItem item: order.getOrderItems()){
            String pName = item.getProduct() != null ? item.getProduct().getName() : "[Deleted Product]";
            OrderItemResponse orderItemResponse = new OrderItemResponse(
                pName,
                item.getQuantity(),
                item.getTotalPrice()
            );
            itemResponses.add(orderItemResponse);

        }

        OrderResponse orderResponse = new OrderResponse(saveOrder.getOrderId(),
                                            saveOrder.getCustomerName(),
                                            saveOrder.getEmail(),
                                            saveOrder.getStatus(),
                                            saveOrder.getOrderDate(),
                                            itemResponses
                                        );
        return orderResponse;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrderResponses() {

        List<Order> orders = orderRepo.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();

        for(Order order: orders){
            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for(OrderItem item : order.getOrderItems()){
                String pName = item.getProduct() != null ? item.getProduct().getName() : "[Deleted Product]";
                OrderItemResponse orderItemResponse = new OrderItemResponse(
                    pName,
                    item.getQuantity(),
                    item.getTotalPrice()
                );
                itemResponses.add(orderItemResponse);

            }
            OrderResponse orderResponse = new OrderResponse(order.getOrderId(),
                                            order.getCustomerName(),
                                            order.getEmail(),
                                            order.getStatus(),
                                            order.getOrderDate(),
                                            itemResponses
                                        );
            orderResponses.add(orderResponse);
        }
        return orderResponses;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(String username, boolean isAdmin) {
        if (isAdmin) {
            return getAllOrderResponses();
        }

        if (username == null || username.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Order> allOrders = orderRepo.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Order order : allOrders) {
            boolean isMatch = false;
            if (order.getUsername() != null && order.getUsername().equalsIgnoreCase(username)) {
                isMatch = true;
            }
            if (!isMatch && order.getCustomerName() != null && order.getCustomerName().equalsIgnoreCase(username)) {
                isMatch = true;
            }
            if (!isMatch && order.getEmail() != null && order.getEmail().equalsIgnoreCase(username)) {
                isMatch = true;
            }

            if (isMatch) {
                List<OrderItemResponse> itemResponses = new ArrayList<>();
                for (OrderItem item : order.getOrderItems()) {
                    String pName = item.getProduct() != null ? item.getProduct().getName() : "[Deleted Product]";
                    OrderItemResponse orderItemResponse = new OrderItemResponse(
                        pName,
                        item.getQuantity(),
                        item.getTotalPrice()
                    );
                    itemResponses.add(orderItemResponse);
                }
                orderResponses.add(new OrderResponse(
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    itemResponses
                ));
            }
        }
        return orderResponses;
    }

    public OrderResponse updateOrderStatus(String orderId, String newStatus) {
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(newStatus.toUpperCase());
        Order updated = orderRepo.save(order);

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : updated.getOrderItems()) {
            String pName = item.getProduct() != null ? item.getProduct().getName() : "[Deleted Product]";
            itemResponses.add(new OrderItemResponse(
                pName,
                item.getQuantity(),
                item.getTotalPrice()
            ));
        }

        return new OrderResponse(
            updated.getOrderId(),
            updated.getCustomerName(),
            updated.getEmail(),
            updated.getStatus(),
            updated.getOrderDate(),
            itemResponses
        );
    }
}
