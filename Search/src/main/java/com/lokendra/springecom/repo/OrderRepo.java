package com.lokendra.springecom.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lokendra.springecom.model.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order, Integer>{

    Optional<Order> findByOrderId(String orderId);

    Optional<Order> findByOrderIdIgnoreCase(String orderId);

    List<Order> findByEmailIgnoreCase(String email);

    List<Order> findByCustomerNameContainingIgnoreCase(String customerName);

    @Override
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findAll();
}
