package com.lokendra.springecom.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "orders")
@Getter
@Setter
@ToString
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderId;

    private String customerName;
    private String email;
    private String status;
    private LocalDate orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<OrderItem> orderItems;

    private String username;

    public Order() {
    }

    public Order(Long id, String orderId, String customerName, String email, String status, LocalDate orderDate, List<OrderItem> orderItems) {
        this.id = id;
        this.orderId = orderId;
        this.customerName = customerName;
        this.email = email;
        this.status = status;
        this.orderDate = orderDate;
        this.orderItems = orderItems;
    }

    public Order(Long id, String orderId, String customerName, String email, String status, LocalDate orderDate, List<OrderItem> orderItems, String username) {
        this.id = id;
        this.orderId = orderId;
        this.customerName = customerName;
        this.email = email;
        this.status = status;
        this.orderDate = orderDate;
        this.orderItems = orderItems;
        this.username = username;
    }
}
