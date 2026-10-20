package com.lokendra.springecom.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Product product;

    private int quantity;

    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Order order;

    public OrderItem() {
    }

    public OrderItem(int id, Product product, int quantity, BigDecimal totalPrice, Order order) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.order = order;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private Product product;
        private int quantity;
        private BigDecimal totalPrice;
        private Order order;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder product(Product product) {
            this.product = product;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public Builder order(Order order) {
            this.order = order;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(id, product, quantity, totalPrice, order);
        }
    }
}