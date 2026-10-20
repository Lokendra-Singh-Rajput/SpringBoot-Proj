package com.lokendra.springecom.service;

import com.google.genai.Client;
import com.lokendra.springecom.model.Order;
import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.model.dto.AgentAction;
import com.lokendra.springecom.model.dto.AgentResponse;
import com.lokendra.springecom.model.dto.ToolCallRecord;
import com.lokendra.springecom.repo.OrderRepo;
import com.lokendra.springecom.repo.ProductRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private static final int DEFAULT_TOP_K = 6;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are "Lokendra AI", the elite autonomous shopping concierge for Lokendra Store (India's premier e-commerce platform).

            OPERATING INSTRUCTIONS:
            1. If the customer asks about store rules, shipping, returns, refunds, payments, warranty, delivery times, or how to manage their cart, answer clearly and accurately using the STORE POLICIES CONTEXT below.
            2. If the customer is shopping for products, ONLY recommend items present in the GROUNDED CATALOG CONTEXT. Do not fabricate products or specifications. Include citation format: [Source: <Product Name> | ID: <ID> | ₹<Price>].
            3. Always format currency in Indian Rupees (₹) with appropriate commas (e.g. ₹1,49,990).
            4. If the customer's request cannot be completed directly (such as clearing cart or unsupported actions), explain kindly and guide them to the right place on the website (e.g. "You can manage or clear items anytime by visiting the Cart page at the top right").
            5. If the inquiry is off-topic or unrelated to the store or products, politely explain your scope as the store shopping assistant.
            6. Keep your tone professional, energetic, transparent, and direct.

            ════════════════════════════════════════
            AGENT TOOL EXECUTION TRACE:
            %s
            ════════════════════════════════════════

            ════════════════════════════════════════
            STORE POLICIES & CUSTOMER SERVICE CONTEXT:
            %s
            ════════════════════════════════════════

            ════════════════════════════════════════
            GROUNDED CATALOG & ORDER CONTEXT:
            %s
            ════════════════════════════════════════
            """;

    @Value("${spring.ai.google.genai.api-key:}")
    private String geminiApiKey;

    @Value("${spring.ai.google.genai.chat.model:gemini-flash-latest}")
    private String geminiChatModel;

    @Autowired
    private StorePolicyService storePolicyService;

    @Autowired
    private ProductVectorService vectorService;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderRepo orderRepo;

    private Client genAiClient;

    private synchronized Client getGenAiClient() {
        if (genAiClient == null && geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                genAiClient = Client.builder().apiKey(geminiApiKey.trim()).build();
            } catch (Exception e) {
                log.warn("Could not create Gemini Client: {}", e.getMessage());
            }
        }
        return genAiClient;
    }

    public AgentResponse chatWithAgent(String message) {
        log.info("Agent received query: '{}'", message);

        List<ToolCallRecord> toolCalls = new ArrayList<>();
        List<AgentAction> actions = new ArrayList<>();
        List<String> citations = new ArrayList<>();
        StringBuilder thoughtProcess = new StringBuilder();

        String lowerQuery = message.toLowerCase().trim();

        thoughtProcess.append("1. [Intent Analysis] Analyzing conversational intent, constraints, and store knowledge scope.\n");

        BigDecimal maxPrice = extractMaxPrice(lowerQuery);
        BigDecimal minPrice = extractMinPrice(lowerQuery);
        String category = extractCategory(lowerQuery);

        boolean isAddToCart = lowerQuery.contains("add to cart") || lowerQuery.contains("put in cart") || lowerQuery.contains("buy this") || lowerQuery.startsWith("add ");
        boolean isClearCart = lowerQuery.contains("cart") && (lowerQuery.contains("remove") || lowerQuery.contains("clear") || lowerQuery.contains("empty") || lowerQuery.contains("delete"));
        boolean isOrderQuery = lowerQuery.contains("order") || lowerQuery.contains("track") || lowerQuery.contains("shipment") || lowerQuery.contains("status");
        boolean isCouponQuery = lowerQuery.contains("coupon") || lowerQuery.contains("discount") || lowerQuery.contains("promo");
        boolean isCompareQuery = lowerQuery.contains("compare") || lowerQuery.contains("versus") || lowerQuery.contains("vs ") || lowerQuery.contains("difference between");
        boolean isPolicyQuery = storePolicyService.isPolicyOrStoreInquiry(lowerQuery);
        boolean isProductSearch = !isClearCart && (isAddToCart || isCompareQuery || category != null || maxPrice != null || minPrice != null || containsProductSearchIntent(lowerQuery));

        StringBuilder catalogContextBuilder = new StringBuilder();
        StringBuilder policyContextBuilder = new StringBuilder();

        if (isPolicyQuery || isClearCart) {
            thoughtProcess.append("2. [Policy RAG] Query relates to store guidelines or operation. Triggering tool: searchPolicies.\n");
            String retrievedPolicy = storePolicyService.retrieveMatchingPolicies(message);
            policyContextBuilder.append(retrievedPolicy);
            toolCalls.add(new ToolCallRecord(
                    "searchPolicies",
                    Map.of("query", message),
                    "Retrieved relevant store policy clauses from verified policy knowledge base.",
                    "SUCCESS"
            ));
        }

        if (isOrderQuery) {
            thoughtProcess.append("3. [Tool Selection] Detected order inquiry. Triggering tool: checkOrderStatus.\n");
            String orderSummary = executeCheckOrderStatus(message, toolCalls, actions);
            if (!orderSummary.isEmpty()) {
                catalogContextBuilder.append("ORDER TRACKING CONTEXT:\n").append(orderSummary).append("\n\n");
            }
        }

        if (isCouponQuery) {
            thoughtProcess.append("4. [Tool Selection] Detected discount inquiry. Triggering tool: applyCoupon.\n");
            executeApplyCoupon(lowerQuery, toolCalls, actions, catalogContextBuilder);
        }

        List<Product> retrievedProducts = new ArrayList<>();
        if (isProductSearch) {
            thoughtProcess.append("5. [Hybrid Catalog RAG] Triggering tool: searchProducts with dense vectors & relational constraints.\n");
            retrievedProducts = executeHybridSearch(message, category, minPrice, maxPrice, toolCalls);

            if (!retrievedProducts.isEmpty()) {
                catalogContextBuilder.append("GROUNDED PRODUCT CATALOG:\n");
                for (Product p : retrievedProducts) {
                    String citation = String.format("[Source: %s | ID: %d | ₹%s]",
                            p.getName(), p.getId(), p.getPrice() != null ? p.getPrice().toPlainString() : "0");
                    citations.add(citation);

                    catalogContextBuilder.append(String.format("- Product #%d: %s | Brand: %s | Category: %s | Price: ₹%s | Stock: %d | Available: %s\n  Description: %s\n",
                            p.getId(), p.getName(), p.getBrand(), p.getCategory(),
                            p.getPrice() != null ? p.getPrice().toPlainString() : "0",
                            p.getStockQuantity(), p.isProductAvailable(), p.getDescription()));
                }
            } else {
                catalogContextBuilder.append("No catalog products strictly matching current filters.\n");
            }

            if (isCompareQuery && retrievedProducts.size() >= 2) {
                thoughtProcess.append("6. [Tool Selection] Detected comparative intent. Triggering tool: compareProducts.\n");
                executeCompareProducts(retrievedProducts.get(0), retrievedProducts.get(1), toolCalls, catalogContextBuilder);
            }

            if (isAddToCart) {
                thoughtProcess.append("7. [Tool Selection] Detected cart modification intent. Triggering tool: addToCart.\n");
                executeAddToCart(message, retrievedProducts, toolCalls, actions);
            }
        }

        thoughtProcess.append("8. [Grounded Synthesis] Synthesizing intelligent natural language response.\n");

        String toolExecutionTrace = formatToolTrace(toolCalls);
        String systemPrompt = String.format(
                SYSTEM_PROMPT_TEMPLATE,
                toolExecutionTrace,
                policyContextBuilder.length() > 0 ? policyContextBuilder.toString() : storePolicyService.getAllPoliciesSummary(),
                catalogContextBuilder.toString()
        );

        String replyText = callGeminiLlm(systemPrompt, message);
        if (replyText == null || replyText.isBlank()) {
            replyText = buildFallbackResponse(message, retrievedProducts, actions, isAddToCart, isClearCart, isOrderQuery, isPolicyQuery);
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

    private String callGeminiLlm(String systemPrompt, String userMessage) {
        Client client = getGenAiClient();
        if (client == null) {
            return null;
        }

        try {
            String combinedPrompt = systemPrompt + "\n\nCUSTOMER MESSAGE: " + userMessage + "\n\nLOKENDRA AI RESPONSE:";
            return client.models.generateContent(
                    geminiChatModel != null && !geminiChatModel.isBlank() ? geminiChatModel : "gemini-flash-latest",
                    combinedPrompt,
                    null
            ).text();
        } catch (Exception e) {
            log.warn("Gemini LLM call did not succeed, using grounded fallback: {}", e.getMessage());
            return null;
        }
    }

    private boolean containsProductSearchIntent(String q) {
        String[] keywords = {
                "find", "show", "search", "buy", "looking for", "recommend", "laptop", "phone", "mobile",
                "headphone", "earphone", "macbook", "electronics", "price", "budget", "under", "spec",
                "cheap", "best", "available", "store", "product", "item", "gadget", "brand"
        };
        for (String kw : keywords) {
            if (q.contains(kw)) return true;
        }
        return false;
    }

    private String buildFallbackResponse(String message, List<Product> products, List<AgentAction> actions,
                                          boolean isCart, boolean isClearCart, boolean isOrder, boolean isPolicy) {
        if (isClearCart) {
            return "🗑️ **Cart Management**: You can manage and empty your cart anytime directly from the **Cart** page (click the Cart icon at the top right of your screen) where you can remove individual items or clear the whole cart with one click!";
        }

        if (isCart && !actions.isEmpty()) {
            return "🛒 I have successfully added the selected product directly to your cart!";
        }

        if (isPolicy) {
            return storePolicyService.buildPolicyDirectAnswer(message);
        }

        if (!products.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Here are verified products matching your request:\n");
            for (Product p : products) {
                sb.append(String.format("• **%s** — ₹%s (%s, Stock: %d)\n",
                        p.getName(), p.getPrice(), p.getBrand(), p.getStockQuantity()));
            }
            return sb.toString();
        }

        if (isOrder) {
            return "To check your order status, please provide your Order ID (e.g. ORD5DB06215) or visit the Orders page in the top menu.";
        }

        String lower = message.toLowerCase();
        if (lower.contains("hi") || lower.contains("hello") || lower.contains("hey")) {
            return "👋 Hello! I am your Lokendra Store AI Concierge. I can help you search our catalog, track orders, check store policies (free shipping above ₹499, 7-day replacements), and apply promotional discount codes!";
        }

        return "I am your Lokendra Store AI assistant. I can help you search for products, check 7-day return policies, track orders, or find current discounts. Please let me know what product or store policy you'd like help with!";
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
            } else if (minPrice == null && maxPrice == null && category == null) {
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
        String clean = text != null ? text.replace(",", "") : "";
        Pattern lakhPattern = Pattern.compile("(?i)(?:under|below|less than|max|within)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:lakh|lac|l)\\b");
        Matcher lakhMatcher = lakhPattern.matcher(clean);
        if (lakhMatcher.find()) {
            double val = Double.parseDouble(lakhMatcher.group(1));
            return BigDecimal.valueOf((long) (val * 100000));
        }

        Pattern kPattern = Pattern.compile("(?i)(?:under|below|less than|max|within)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]+)\\s*k\\b");
        Matcher kMatcher = kPattern.matcher(clean);
        if (kMatcher.find()) {
            long val = Long.parseLong(kMatcher.group(1));
            return BigDecimal.valueOf(val * 1000);
        }

        Pattern numPattern = Pattern.compile("(?i)(?:under|below|less than|max|within)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]{3,7})\\b");
        Matcher numMatcher = numPattern.matcher(clean);
        if (numMatcher.find()) {
            return new BigDecimal(numMatcher.group(1));
        }
        return null;
    }

    private BigDecimal extractMinPrice(String text) {
        String clean = text != null ? text.replace(",", "") : "";
        Pattern p = Pattern.compile("(?i)(?:above|over|more than|min|at least)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]+)\\s*(?:k|000)?\\b");
        Matcher m = p.matcher(clean);
        if (m.find()) {
            String raw = m.group(1);
            long val = Long.parseLong(raw);
            if (clean.toLowerCase().contains(raw + "k")) {
                val *= 1000;
            }
            return BigDecimal.valueOf(val);
        }
        return null;
    }

    private String extractCategory(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("laptop") || lower.contains("macbook") || lower.contains("notebook")) return "Laptop";
        if (lower.contains("headphone") || lower.contains("earphone") || lower.contains("audio") || lower.contains("earbuds")) return "Headphone";
        if (lower.contains("mobile") || lower.contains("phone") || lower.contains("smartphone") || lower.contains("iphone")) return "Mobile";
        if (lower.contains("electronics") || lower.contains("gadget")) return "Electronics";
        if (lower.contains("toy") || lower.contains("kids")) return "Toys";
        if (lower.contains("fashion") || lower.contains("clothes") || lower.contains("wear")) return "Fashion";
        return null;
    }

    private int extractQuantity(String text) {
        Pattern p = Pattern.compile("\\b([1-9]|10)\\b\\s*(?:units?|pieces?|items?)?");
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 1;
    }

    private boolean matchesPriceBounds(Product p, BigDecimal min, BigDecimal max) {
        if (p == null || p.getPrice() == null) return false;
        if (min != null && p.getPrice().compareTo(min) < 0) return false;
        if (max != null && p.getPrice().compareTo(max) > 0) return false;
        return true;
    }

    private String formatOrderDetails(Order ord) {
        return String.format("Order #%s | Status: %s | Placed: %s | Customer: %s",
                ord.getOrderId(), ord.getStatus(), ord.getOrderDate(), ord.getCustomerName());
    }

    private String formatToolTrace(List<ToolCallRecord> records) {
        if (records.isEmpty()) return "No external tools invoked for this turn.";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.size(); i++) {
            ToolCallRecord r = records.get(i);
            sb.append(String.format("Step %d: Tool '%s' -> %s [Status: %s]\n",
                    i + 1, r.toolName(), r.resultSummary(), r.status()));
        }
        return sb.toString();
    }

    public List<Product> semanticSearch(String query, int topK) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String lowerQuery = query.toLowerCase().trim();
        BigDecimal maxPrice = extractMaxPrice(lowerQuery);
        BigDecimal minPrice = extractMinPrice(lowerQuery);
        String category = extractCategory(lowerQuery);

        List<ToolCallRecord> ignoredCalls = new ArrayList<>();
        List<Product> products = executeHybridSearch(query, category, minPrice, maxPrice, ignoredCalls);

        if (maxPrice != null || minPrice != null) {
            products = products.stream()
                    .filter(p -> matchesPriceBounds(p, minPrice, maxPrice))
                    .collect(Collectors.toList());
        }

        if (category != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        int limit = topK <= 0 ? DEFAULT_TOP_K : topK;
        return products.stream().limit(limit).collect(Collectors.toList());
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
