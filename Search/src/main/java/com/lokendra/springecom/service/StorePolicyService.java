package com.lokendra.springecom.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StorePolicyService {

    public record PolicyDocument(String title, String category, List<String> keywords, String content) {}

    private final List<PolicyDocument> policies = new ArrayList<>();

    public StorePolicyService() {
        initPolicies();
    }

    private void initPolicies() {
        policies.add(new PolicyDocument(
                "Shipping & Delivery Policy",
                "SHIPPING",
                List.of("ship", "shipping", "deliver", "delivery", "dispatch", "courier", "timeline", "charges", "free delivery", "track shipment", "days"),
                "• Free standard delivery is provided on all orders above ₹499 across India. For orders below ₹499, a flat delivery fee of ₹49 applies.\n" +
                "• Metro cities (Delhi NCR, Mumbai, Bengaluru, Hyderabad, Chennai, Kolkata): Delivered within 2-3 business days.\n" +
                "• Non-metro and regional locations: Delivered within 4-7 business days.\n" +
                "• Trusted logistics partners: Blue Dart, Delhivery, DTDC, and India Post.\n" +
                "• Real-time SMS and email tracking links are dispatched immediately after packaging."
        ));

        policies.add(new PolicyDocument(
                "Returns & Replacement Policy",
                "RETURNS",
                List.of("return", "returns", "replace", "replacement", "exchange", "damaged", "defective", "broken", "wrong item", "faulty"),
                "• 7-Day Replacement Window: Customers may initiate a return or replacement within 7 calendar days from delivery for defective, damaged, or incorrect items.\n" +
                "• Eligibility: The product must be unused, in original condition with manufacturer packaging, invoice, warranty card, and all included accessories intact.\n" +
                "• Electronics & Gadgets: In case of technical defects or dead-on-arrival units, our brand support team schedules a home inspection or direct brand-authorized replacement within 48 hours."
        ));

        policies.add(new PolicyDocument(
                "Refund Policy",
                "REFUNDS",
                List.of("refund", "refunds", "money back", "reimbursement", "bank transfer", "account credit"),
                "• Refunds are initiated within 24-48 hours after the returned product passes quality inspection at our fulfillment hub.\n" +
                "• Pre-paid orders (Cards, UPI, NetBanking): Credited back to the original source payment method within 5-7 business days.\n" +
                "• Cash on Delivery (COD) orders: Refunded via secure UPI transfer or direct NEFT/IMPS bank transfer as chosen by the customer."
        ));

        policies.add(new PolicyDocument(
                "Order Cancellation Policy",
                "CANCELLATION",
                List.of("cancel", "cancellation", "abort order", "stop order", "undo order"),
                "• Customers can cancel their order free of charge at any moment prior to dispatch directly from the 'Orders' dashboard.\n" +
                "• Once dispatched, an order cannot be cancelled in-transit, but the customer may simply refuse delivery at doorstep without any penalty."
        ));

        policies.add(new PolicyDocument(
                "Payment Methods & Security",
                "PAYMENTS",
                List.of("payment", "pay", "cod", "cash on delivery", "upi", "card", "credit card", "debit card", "emi", "net banking", "gpay", "phonepe", "paytm", "secure"),
                "• We accept all major payment methods: UPI (Google Pay, PhonePe, Paytm, BHIM), Credit/Debit Cards (Visa, MasterCard, RuPay), Net Banking (50+ banks), and Cash on Delivery (COD).\n" +
                "• Payment Security: 100% safe checkout powered by RBI-authorized payment gateways with 256-bit SSL encryption and dual-factor authentication."
        ));

        policies.add(new PolicyDocument(
                "Warranty & Authenticity Guarantee",
                "WARRANTY",
                List.of("warranty", "guarantee", "genuine", "authentic", "original", "fake", "gst", "invoice"),
                "• 100% Genuine Guarantee: All products listed on Lokendra Store are brand-new, original, and sourced directly from authorized brand distributors.\n" +
                "• Standard Manufacturer Warranty: All electronics and lifestyle items carry official 1-year brand warranty honored at official service centers across India.\n" +
                "• Official GST Tax Invoices are provided for all purchases and can be downloaded anytime from your Orders section."
        ));

        policies.add(new PolicyDocument(
                "Cart & Store Management Guidance",
                "CART_MANAGEMENT",
                List.of("cart", "remove from cart", "empty cart", "clear cart", "delete cart", "checkout", "basket"),
                "• Cart Management: Customers can view, adjust quantities, or remove individual items at any time by clicking the 'Cart' icon in the top navigation bar.\n" +
                "• To empty your entire cart, open the Cart page (/cart) and click 'Remove' on items or proceed to checkout.\n" +
                "• Items added to cart remain saved across sessions for verified logged-in accounts."
        ));

        policies.add(new PolicyDocument(
                "Customer Support & Helpdesk",
                "SUPPORT",
                List.of("support", "contact", "help", "email", "phone", "hours", "customer care", "helpline", "reach"),
                "• Email: support@lokendrastore.in | Direct response within 4 working hours.\n" +
                "• Helpline: 1800-200-LOKENDRA (Toll Free, Mon-Sat 9:00 AM – 8:00 PM IST).\n" +
                "• Live Assistant: Lokendra Agentic Concierge is available 24/7 in your browser."
        ));
    }

    public boolean isPolicyOrStoreInquiry(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        for (PolicyDocument doc : policies) {
            for (String kw : doc.keywords()) {
                if (lower.contains(kw)) {
                    return true;
                }
            }
        }
        return lower.contains("policy") || lower.contains("rules") || lower.contains("terms") || lower.contains("how to");
    }

    public String retrieveMatchingPolicies(String query) {
        if (query == null || query.isBlank()) return "";
        String lower = query.toLowerCase();

        List<PolicyDocument> matches = new ArrayList<>();
        for (PolicyDocument doc : policies) {
            int score = 0;
            for (String kw : doc.keywords()) {
                if (lower.contains(kw)) {
                    score++;
                }
            }
            if (score > 0) {
                matches.add(doc);
            }
        }

        if (matches.isEmpty()) {
            return getAllPoliciesSummary();
        }

        StringBuilder sb = new StringBuilder();
        for (PolicyDocument doc : matches) {
            sb.append("=== ").append(doc.title()).append(" ===\n");
            sb.append(doc.content()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public String getAllPoliciesSummary() {
        StringBuilder sb = new StringBuilder();
        for (PolicyDocument doc : policies) {
            sb.append("=== ").append(doc.title()).append(" ===\n");
            sb.append(doc.content()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public String buildPolicyDirectAnswer(String query) {
        String lower = query.toLowerCase();
        if (lower.contains("cart") && (lower.contains("remove") || lower.contains("clear") || lower.contains("empty") || lower.contains("delete"))) {
            return "🗑️ **Cart Management**: You can manage and clear your items at any time by opening the **Cart** page (click the Cart icon at the top right of your screen). From there, you can adjust quantities or remove items with one click!";
        }
        if (lower.contains("return") || lower.contains("replace") || lower.contains("exchange") || lower.contains("damaged")) {
            return "📦 **Returns & Replacements**: Lokendra Store offers a **7-Day Hassle-Free Replacement Policy** for any defective, damaged, or incorrect items. Products must be unused with original packaging and invoice intact. Brand technicians handle electronic replacements within 48 hours.";
        }
        if (lower.contains("refund")) {
            return "💳 **Refund Policy**: Once returned items pass quality inspection, refunds are processed within 24–48 hours. Pre-paid orders are credited to your original payment method in 5–7 business days. COD orders are refunded via direct UPI or bank transfer.";
        }
        if (lower.contains("ship") || lower.contains("deliver") || lower.contains("dispatch")) {
            return "🚚 **Shipping & Delivery**: We offer **Free Delivery on orders above ₹499** (₹49 for orders below ₹499). Metro cities receive orders in 2–3 business days; other regions in 4–7 business days via Blue Dart, Delhivery, and DTDC.";
        }
        if (lower.contains("cancel")) {
            return "❌ **Order Cancellation**: You can cancel orders free of charge anytime before dispatch directly from your **Orders** dashboard. If already dispatched, you can simply decline delivery at your doorstep.";
        }
        if (lower.contains("warranty") || lower.contains("genuine") || lower.contains("authentic")) {
            return "🛡️ **Warranty & Authenticity**: All products on Lokendra Store are 100% genuine and brand new, backed by official **1-Year Manufacturer Warranty** and GST invoices.";
        }
        if (lower.contains("pay") || lower.contains("cod") || lower.contains("upi")) {
            return "💰 **Payment Options**: We support UPI (Google Pay, PhonePe, Paytm), Credit/Debit Cards, Net Banking, and **Cash on Delivery (COD)** with 256-bit bank-grade SSL encryption.";
        }
        return "ℹ️ **Lokendra Store Assistance**: I am your store concierge. I can assist you with product searches, 7-day replacements, free delivery above ₹499, order tracking, and promotional coupons!";
    }
}
