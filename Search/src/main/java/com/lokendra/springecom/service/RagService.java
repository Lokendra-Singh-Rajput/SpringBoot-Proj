package com.lokendra.springecom.service;

import com.lokendra.springecom.model.Order;
import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.model.dto.AgentAction;
import com.lokendra.springecom.model.dto.AgentResponse;
import com.lokendra.springecom.model.dto.ToolCallRecord;
import com.lokendra.springecom.repo.OrderRepo;
import com.lokendra.springecom.repo.ProductRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private static final int DEFAULT_TOP_K = 6;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are "Lokendra AI", the elite autonomous shopping assistant for Lokendra Store (India's premier electronics & lifestyle e-commerce platform).
            
            Operating Mode: Autonomous Agent with Grounded RAG.
            
            YOUR GUIDELINES:
            1. ONLY recommend products that exist in the CATALOG CONTEXT below. Do not fabricate products, specs, or prices.
            2. Always format prices in Indian Rupees (₹) with appropriate commas (e.g. ₹1,49,990).
            3. When you refer to any product, include its citation badge format: [Source: <Product Name> | ID: <ID> | ₹<Price>].
            4. If a tool was executed (like adding an item to the cart, applying a coupon, or checking an order), clearly acknowledge the action to the customer.
            5. If a product is out of stock (stock = 0), explicitly warn the customer and suggest an in-stock alternative.
            6. Keep your tone professional, energetic, transparent, and direct.
            
            ════════════════════════════════════════
            AGENT TOOL EXECUTION TRACE:
            %s
            ════════════════════════════════════════
            
            ════════════════════════════════════════
            RETRIEVED CATALOG & ORDER CONTEXT:
            %s
            ════════════════════════════════════════
            """;

    @Autowired
    @Qualifier("ollamaChatModel")
    private org.springframework.ai.chat.model.ChatModel chatModel;

    @org.springframework.beans.factory.annotation.Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Autowired
    private ProductVectorService vectorService;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderRepo orderRepo;

    

    
    public AgentResponse chatWithAgent(String message) {
        log.info("Agent received query: '{}'", message);

        List<ToolCallRecord> toolCalls = new ArrayList<>();
        List<AgentAction> actions = new ArrayList<>();
        List<String> citations = new ArrayList<>();
        StringBuilder thoughtProcess = new StringBuilder();

        String lowerQuery = message.toLowerCase().trim();

        
        thoughtProcess.append("1. [Intent Analysis] Analyzing natural language intent and numerical constraints.\n");

        BigDecimal maxPrice = extractMaxPrice(lowerQuery);
        BigDecimal minPrice = extractMinPrice(lowerQuery);
        String category = extractCategory(lowerQuery);
        boolean isAddToCart = lowerQuery.contains("add to cart") || lowerQuery.contains("put in cart") || lowerQuery.contains("buy this") || lowerQuery.startsWith("add ");
        boolean isOrderQuery = lowerQuery.contains("order") || lowerQuery.contains("track") || lowerQuery.contains("shipment") || lowerQuery.contains("status");
        boolean isCouponQuery = lowerQuery.contains("coupon") || lowerQuery.contains("discount") || lowerQuery.contains("promo");
        boolean isCompareQuery = lowerQuery.contains("compare") || lowerQuery.contains("versus") || lowerQuery.contains("vs ") || lowerQuery.contains("difference between");

        
        List<Product> retrievedProducts = new ArrayList<>();
        StringBuilder contextBuilder = new StringBuilder();

        
        if (isOrderQuery) {
            thoughtProcess.append("2. [Tool Selection] Detected order inquiry. Triggering tool: checkOrderStatus.\n");
            String orderSummary = executeCheckOrderStatus(message, toolCalls, actions);
            if (!orderSummary.isEmpty()) {
                contextBuilder.append("ORDER TRACKING CONTEXT:\n").append(orderSummary).append("\n\n");
            }
        }

        
        if (isCouponQuery) {
            thoughtProcess.append("2. [Tool Selection] Detected discount inquiry. Triggering tool: applyCoupon.\n");
            executeApplyCoupon(lowerQuery, toolCalls, actions, contextBuilder);
        }

        
        thoughtProcess.append("3. [Hybrid RAG Retrieval] Triggering tool: searchProducts with dense vectors & relational constraints.\n");
        retrievedProducts = executeHybridSearch(message, category, minPrice, maxPrice, toolCalls);

        if (!retrievedProducts.isEmpty()) {
            contextBuilder.append("GROUNDED PRODUCT CATALOG:\n");
            for (Product p : retrievedProducts) {
                String citation = String.format("[Source: %s | ID: %d | ₹%s]",
                        p.getName(), p.getId(), p.getPrice() != null ? p.getPrice().toPlainString() : "0");
                citations.add(citation);

                contextBuilder.append(String.format("- Product #%d: %s | Brand: %s | Category: %s | Price: ₹%s | Stock: %d | Available: %s\n  Description: %s\n",
                        p.getId(), p.getName(), p.getBrand(), p.getCategory(),
                        p.getPrice() != null ? p.getPrice().toPlainString() : "0",
                        p.getStockQuantity(), p.isProductAvailable(), p.getDescription()));
            }
        } else {
            contextBuilder.append("No catalog products strictly matching current filters.\n");
        }

        
        if (isCompareQuery && retrievedProducts.size() >= 2) {
            thoughtProcess.append("4. [Tool Selection] Detected comparative intent. Triggering tool: compareProducts.\n");
            executeCompareProducts(retrievedProducts.get(0), retrievedProducts.get(1), toolCalls, contextBuilder);
        }

        
        if (isAddToCart) {
            thoughtProcess.append("5. [Tool Selection] Detected cart modification intent. Triggering tool: addToCart.\n");
            executeAddToCart(message, retrievedProducts, toolCalls, actions);
        }

        thoughtProcess.append("6. [Grounded Synthesis] Synthesizing final response through LLM with fact verification.\n");

        
        String toolExecutionTrace = formatToolTrace(toolCalls);
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, toolExecutionTrace, contextBuilder.toString());

        String replyText;
        if (isOllamaReachable()) {
            try {
                ChatClient chatClient = ChatClient.create(chatModel);
                replyText = chatClient.prompt()
                        .system(systemPrompt)
                        .user(message)
                        .call()
                        .chatResponse()
                        .getResult()
                        .getOutput()
                        .getText();
            } catch (Exception e) {
                log.error("LLM Generation failed: {}", e.getMessage(), e);
                replyText = buildFallbackResponse(retrievedProducts, actions, isAddToCart, isOrderQuery);
            }
        } else {
            replyText = buildFallbackResponse(retrievedProducts, actions, isAddToCart, isOrderQuery);
        }

        return new AgentResponse(
                replyText,
                thoughtProcess.toString(),
                toolCalls,
                actions,
                retrievedProducts,
                citations
        );
    }

    

    public RagResponse chat(String message) {
        AgentResponse agentResponse = chatWithAgent(message);
        return new RagResponse(agentResponse.reply(), agentResponse.products());
    }

    

    private List<Product> executeHybridSearch(String query, String category,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              List<ToolCallRecord> toolCalls) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("query", query);
        if (category != null) args.put("category", category);
        if (minPrice != null) args.put("minPrice", minPrice);
        if (maxPrice != null) args.put("maxPrice", maxPrice);

        
        List<Product> vectorProducts = new ArrayList<>();
        try {
            List<Document> vectorDocs = vectorService.semanticSearch(query, DEFAULT_TOP_K);
            vectorProducts = resolveProducts(vectorDocs);
        } catch (Exception e) {
            log.warn("Hybrid search vector step skipped: {}", e.getMessage());
        }

        List<Product> sqlFiltered = productRepo.filterProducts(category, minPrice, maxPrice, false);
        if (sqlFiltered.isEmpty() && query != null && !query.isBlank()) {
            sqlFiltered = smartKeywordSearch(query, DEFAULT_TOP_K);
            if (minPrice != null || maxPrice != null) {
                sqlFiltered = sqlFiltered.stream()
                        .filter(p -> matchesPriceBounds(p, minPrice, maxPrice))
                        .collect(Collectors.toList());
            }
        }

        Set<Integer> sqlIds = sqlFiltered.stream().map(Product::getId).collect(Collectors.toSet());
        List<Product> result = new ArrayList<>();

        for (Product p : vectorProducts) {
            if (sqlIds.isEmpty() || sqlIds.contains(p.getId())) {
                if (matchesPriceBounds(p, minPrice, maxPrice)) {
                    result.add(p);
                }
            }
        }

        if (result.isEmpty()) {
            if (!sqlFiltered.isEmpty()) {
                result.addAll(sqlFiltered.stream().limit(DEFAULT_TOP_K).toList());
            } else {
                result.addAll(vectorProducts);
            }
        }

        
        List<Product> finalProducts = result.stream().distinct().limit(DEFAULT_TOP_K).collect(Collectors.toList());

        toolCalls.add(new ToolCallRecord(
                "searchProducts",
                args,
                String.format("Retrieved %d verified products using Hybrid RAG (Dense Vector + SQL constraints)", finalProducts.size()),
                "SUCCESS"
        ));

        return finalProducts;
    }

    private void executeAddToCart(String message, List<Product> products,
                                  List<ToolCallRecord> toolCalls, List<AgentAction> actions) {
        Map<String, Object> args = new LinkedHashMap<>();
        int quantity = extractQuantity(message);
        args.put("requestedQuantity", quantity);

        Product targetProduct = null;

        
        for (Product p : products) {
            if (message.toLowerCase().contains(p.getName().toLowerCase()) ||
                message.toLowerCase().contains(p.getBrand().toLowerCase())) {
                targetProduct = p;
                break;
            }
        }

        
        if (targetProduct == null && !products.isEmpty()) {
            targetProduct = products.get(0);
        }

        if (targetProduct != null) {
            args.put("productId", targetProduct.getId());
            args.put("productName", targetProduct.getName());

            if (targetProduct.getStockQuantity() <= 0) {
                toolCalls.add(new ToolCallRecord(
                        "addToCart",
                        args,
                        "Failed: Product '" + targetProduct.getName() + "' is Out of Stock.",
                        "FAILED"
                ));
            } else {
                AgentAction action = AgentAction.addToCart(
                        targetProduct,
                        quantity,
                        String.format("Added %d x %s (₹%s) to cart.", quantity, targetProduct.getName(), targetProduct.getPrice())
                );
                actions.add(action);

                toolCalls.add(new ToolCallRecord(
                        "addToCart",
                        args,
                        String.format("Successfully added %d unit(s) of '%s' (ID: %d) to user cart.", quantity, targetProduct.getName(), targetProduct.getId()),
                        "SUCCESS"
                ));
            }
        } else {
            toolCalls.add(new ToolCallRecord(
                    "addToCart",
                    args,
                    "Failed: Could not identify target product from catalog.",
                    "FAILED"
            ));
        }
    }

    private String executeCheckOrderStatus(String message, List<ToolCallRecord> toolCalls, List<AgentAction> actions) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> args = new HashMap<>();

        Pattern pattern = Pattern.compile("(?i)ORD[-_]?[A-Z0-9]{4,12}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String token = matcher.group().toUpperCase().replace("-", "").replace("_", "");
            args.put("orderId", token);
            orderRepo.findByOrderIdIgnoreCase(token).ifPresent(ord -> {
                sb.append(formatOrderDetails(ord)).append("\n");
                actions.add(AgentAction.checkOrder(ord.getOrderId(), "Tracking details for order " + ord.getOrderId()));
            });
        }

        if (!sb.isEmpty()) {
            toolCalls.add(new ToolCallRecord(
                    "checkOrderStatus",
                    args,
                    "Order found in database. Retrieved status and fulfillment timeline.",
                    "SUCCESS"
            ));
            return sb.toString();
        }

        toolCalls.add(new ToolCallRecord(
                "checkOrderStatus",
                args,
                "No specific Order ID found. Prompted customer for ORD-number.",
                "SUCCESS"
        ));
        return "CUSTOMER REQUESTED ORDER STATUS WITHOUT SPECIFYING VALID ORDER ID. Ask them to share their Order ID (e.g. ORDxxxxxxxx).";
    }

    private void executeApplyCoupon(String query, List<ToolCallRecord> toolCalls, List<AgentAction> actions, StringBuilder context) {
        Map<String, Object> args = new HashMap<>();
        if (query.contains("welcome10")) {
            args.put("code", "WELCOME10");
            actions.add(AgentAction.applyCoupon("WELCOME10", 10.0, "10% Welcome Discount applied!"));
            toolCalls.add(new ToolCallRecord("applyCoupon", args, "Applied coupon WELCOME10 for 10% discount.", "SUCCESS"));
            context.append("COUPON VALIDATED: WELCOME10 gives 10% instant discount on total order.\n");
        } else if (query.contains("save500")) {
            args.put("code", "SAVE500");
            actions.add(AgentAction.applyCoupon("SAVE500", 500.0, "Flat ₹500 discount applied!"));
            toolCalls.add(new ToolCallRecord("applyCoupon", args, "Applied coupon SAVE500 for flat ₹500 off.", "SUCCESS"));
            context.append("COUPON VALIDATED: SAVE500 gives flat ₹500 off on orders above ₹2000.\n");
        } else {
            args.put("availableCoupons", List.of("WELCOME10", "SAVE500", "LOKENDRA50"));
            toolCalls.add(new ToolCallRecord("applyCoupon", args, "Listed available promotional coupons to customer.", "SUCCESS"));
            context.append("AVAILABLE PROMO CODES: WELCOME10 (10% off), SAVE500 (Flat ₹500 off on ₹2,000+ orders), LOKENDRA50 (50% festive discount up to ₹1000).\n");
        }
    }

    private void executeCompareProducts(Product a, Product b, List<ToolCallRecord> toolCalls, StringBuilder context) {
        Map<String, Object> args = Map.of("productA", a.getName(), "productB", b.getName());
        toolCalls.add(new ToolCallRecord("compareProducts", args, "Generated side-by-side technical and price comparison.", "SUCCESS"));
        context.append(String.format("PRODUCT COMPARISON MATRIX:\n- %s (₹%s, %s): %s\n- %s (₹%s, %s): %s\n",
                a.getName(), a.getPrice(), a.getBrand(), a.getDescription(),
                b.getName(), b.getPrice(), b.getBrand(), b.getDescription()));
    }

    

    private BigDecimal extractMaxPrice(String text) {
        
        Pattern lakhPattern = Pattern.compile("(?i)(?:under|below|less than|max|within)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:lakh|lac|l)\\b");
        Matcher lakhMatcher = lakhPattern.matcher(text);
        if (lakhMatcher.find()) {
            double val = Double.parseDouble(lakhMatcher.group(1));
            return BigDecimal.valueOf((long) (val * 100000));
        }

        Pattern kPattern = Pattern.compile("(?i)(?:under|below|less than|max|within)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]+)\\s*k\\b");
        Matcher kMatcher = kPattern.matcher(text);
        if (kMatcher.find()) {
            double val = Double.parseDouble(kMatcher.group(1));
            return BigDecimal.valueOf((long) (val * 1000));
        }

        Pattern stdPattern = Pattern.compile("(?i)(?:under|below|less than|max|within)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]{3,7})");
        Matcher stdMatcher = stdPattern.matcher(text);
        if (stdMatcher.find()) {
            return new BigDecimal(stdMatcher.group(1));
        }

        return null;
    }

    private BigDecimal extractMinPrice(String text) {
        Pattern pattern = Pattern.compile("(?i)(?:above|over|more than|min)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]{3,7})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }

    private String extractCategory(String text) {
        if (text.contains("laptop") || text.contains("notebook") || text.contains("macbook")) return "Laptop";
        if (text.contains("headphone") || text.contains("earphone") || text.contains("earbud") || text.contains("audio")) return "Headphones";
        if (text.contains("mobile") || text.contains("phone") || text.contains("smartphone") || text.contains("iphone")) return "Mobile";
        if (text.contains("watch") || text.contains("smartwatch")) return "Watch";
        if (text.contains("shoe") || text.contains("sneaker") || text.contains("footwear")) return "Shoes";
        return null;
    }

    private int extractQuantity(String text) {
        Pattern pattern = Pattern.compile("\\b([1-9]|10)\\b\\s*(?:units?|pcs?|items?|of)?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (Exception ignored) {}
        }
        return 1;
    }

    private boolean matchesPriceBounds(Product p, BigDecimal min, BigDecimal max) {
        if (p.getPrice() == null) return false;
        if (min != null && p.getPrice().compareTo(min) < 0) return false;
        if (max != null && p.getPrice().compareTo(max) > 0) return false;
        return true;
    }

    private String formatOrderDetails(Order ord) {
        StringBuilder items = new StringBuilder();
        if (ord.getOrderItems() != null) {
            for (var it : ord.getOrderItems()) {
                String name = it.getProduct() != null ? it.getProduct().getName() : "Item";
                items.append(name).append(" (x").append(it.getQuantity()).append(", ₹").append(it.getTotalPrice()).append("); ");
            }
        }
        return String.format("- Order #%s | Status: %s | Date: %s | Customer: %s | Items: [%s]",
                ord.getOrderId(), ord.getStatus(), ord.getOrderDate(), ord.getCustomerName(), items.toString());
    }

    private String formatToolTrace(List<ToolCallRecord> records) {
        if (records.isEmpty()) return "None.";
        StringBuilder sb = new StringBuilder();
        for (var r : records) {
            sb.append(String.format("- Tool [%s] | Status: %s | Summary: %s\n", r.toolName(), r.status(), r.resultSummary()));
        }
        return sb.toString();
    }

    private boolean isOllamaReachable() {
        if (ollamaBaseUrl == null || ollamaBaseUrl.isBlank()) {
            return false;
        }
        try {
            java.net.URI uri = java.net.URI.create(ollamaBaseUrl);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }
            int port = uri.getPort() > 0 ? uri.getPort() : 11434;
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 200);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String buildFallbackResponse(List<Product> products, List<AgentAction> actions, boolean isCart, boolean isOrder) {
        StringBuilder sb = new StringBuilder();
        if (isCart && !actions.isEmpty()) {
            sb.append("🛒 I have successfully added the selected product directly to your cart!\n\n");
        }
        if (!products.isEmpty()) {
            sb.append("Here are verified products matching your request:\n");
            for (Product p : products) {
                sb.append(String.format("• **%s** — ₹%s (%s, Stock: %d)\n",
                        p.getName(), p.getPrice(), p.getBrand(), p.getStockQuantity()));
            }
        } else if (isOrder) {
            sb.append("To check your order status, please provide your Order ID (e.g. ORD5DB06215) or visit the Orders page.");
        } else {
            sb.append("I couldn't find exact catalog items matching your criteria. Try browsing our categories or asking with a different budget.");
        }
        return sb.toString();
    }

    public List<Product> semanticSearch(String query, int topK) {
        int limit = topK <= 0 ? DEFAULT_TOP_K : topK;
        try {
            List<Document> docs = vectorService.semanticSearch(query, limit);
            List<Product> products = resolveProducts(docs);
            if (!products.isEmpty()) {
                return products;
            }
        } catch (Exception e) {
            log.warn("Semantic vector search exception: {}", e.getMessage());
        }
        return smartKeywordSearch(query, limit);
    }

    private List<Product> smartKeywordSearch(String query, int limit) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        List<Product> direct = productRepo.searchProducts(query.trim());
        if (!direct.isEmpty()) {
            return direct.stream().limit(limit).collect(Collectors.toList());
        }
        String clean = query.replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase();
        String[] tokens = clean.split("\\s+");
        Set<String> stopWords = Set.of(
                "the", "a", "an", "for", "in", "on", "at", "to", "under", "below", "above", "over",
                "with", "without", "and", "or", "is", "are", "best", "good", "cheap", "buy", "show",
                "give", "me", "want", "need", "i", "can", "you", "please", "some", "any", "top", "of"
        );
        Set<Integer> seen = new HashSet<>();
        List<Product> matched = new ArrayList<>();
        for (String t : tokens) {
            if (t.length() >= 3 && !stopWords.contains(t)) {
                for (Product p : productRepo.searchProducts(t)) {
                    if (seen.add(p.getId())) {
                        matched.add(p);
                    }
                }
                if (t.endsWith("s") && t.length() > 3) {
                    String singular = t.substring(0, t.length() - 1);
                    if (!stopWords.contains(singular)) {
                        for (Product p : productRepo.searchProducts(singular)) {
                            if (seen.add(p.getId())) {
                                matched.add(p);
                            }
                        }
                    }
                }
            }
        }
        return matched.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Product> resolveProducts(List<Document> docs) {
        return docs.stream()
                .map(doc -> {
                    Object idObj = doc.getMetadata().get("productId");
                    if (idObj == null) return null;
                    try {
                        int id = Integer.parseInt(idObj.toString());
                        return productRepo.findById(id).orElse(null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public record RagResponse(String reply, List<Product> products) {}
}
