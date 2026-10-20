package com.lokendra.springecom.model.dto;

import com.lokendra.springecom.model.Product;

public record AgentAction(
        String type, 
        Product product,
        Integer quantity,
        String orderId,
        String couponCode,
        Double discountAmount,
        String message
) {
    public static AgentAction addToCart(Product product, int quantity, String msg) {
        return new AgentAction("ADD_TO_CART", product, quantity, null, null, null, msg);
    }

    public static AgentAction applyCoupon(String code, double discount, String msg) {
        return new AgentAction("APPLY_COUPON", null, null, null, code, discount, msg);
    }

    public static AgentAction checkOrder(String orderId, String msg) {
        return new AgentAction("CHECK_ORDER", null, null, orderId, null, null, msg);
    }
}
