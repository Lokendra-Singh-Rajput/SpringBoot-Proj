package com.lokendra.springecom.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lokendra.springecom.model.Order;

@Repository

public interface OrderRepo extends JpaRepository<Order, Integer>{

    Optional <Order> findByOrderId(String orderId);
}
