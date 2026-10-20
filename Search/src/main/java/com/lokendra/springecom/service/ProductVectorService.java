package com.lokendra.springecom.service;

import com.lokendra.springecom.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductVectorService {

    private static final Logger log = LoggerFactory.getLogger(ProductVectorService.class);

    @Autowired
    private VectorStore vectorStore;

    @Value("${app.vector-store.persist-path:vector-store.json}")
    private String persistPath;

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    private volatile boolean ollamaAvailable = false;
    private volatile long lastOllamaCheck = 0;

    private boolean isOllamaReachable() {
        long now = System.currentTimeMillis();
        if (now - lastOllamaCheck < 60000) {
            return ollamaAvailable;
        }
        lastOllamaCheck = now;
        try {
            URI uri = URI.create(ollamaBaseUrl);
            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            int port = uri.getPort() != -1 ? uri.getPort() : 11434;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 120);
                ollamaAvailable = true;
            }
        } catch (Exception e) {
            ollamaAvailable = false;
        }
        return ollamaAvailable;
    }

    @Async
    public void indexProduct(Product product) {
        if (!isOllamaReachable()) {
            return;
        }
        try {
            removeProduct(product.getId());

            Document doc = buildDocument(product);
            vectorStore.add(List.of(doc));

            persistSnapshot();

            log.debug("RAG: Indexed product id={} name='{}'", product.getId(), product.getName());
        } catch (Exception e) {
            log.error("RAG: Failed to index product id={}: {}", product.getId(), e.getMessage());
        }
    }

    @Async
    public void removeProduct(int productId) {
        if (!isOllamaReachable()) {
            return;
        }
        try {
            vectorStore.delete(List.of(String.valueOf(productId)));
            persistSnapshot();
            log.debug("RAG: Removed product id={} from vector store", productId);
        } catch (Exception e) {
            log.trace("RAG: Remove product id={} — {}", productId, e.getMessage());
        }
    }

    public void indexAll(List<Product> products) {
        if (!isOllamaReachable() || products.isEmpty()) {
            return;
        }

        List<Document> docs = products.stream()
                .map(this::buildDocument)
                .toList();

        vectorStore.add(docs);
        persistSnapshot();
        log.info("RAG: Bulk-indexed {} products into vector store.", products.size());
    }

    public List<Document> semanticSearch(String query, int topK) {
        if (!isOllamaReachable()) {
            return Collections.emptyList();
        }
        try {
            return vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query)
                            .topK(topK)
                            .build()
            );
        } catch (Exception e) {
            log.warn("Vector search unavailable ({}); returning empty list.", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Document buildDocument(Product product) {
        String text = String.format("""
                Product: %s
                Brand: %s
                Category: %s
                Description: %s
                Price: ₹%.2f
                Available: %s
                Stock: %d units
                """,
                product.getName(),
                product.getBrand() != null ? product.getBrand() : "",
                product.getCategory() != null ? product.getCategory() : "",
                product.getDescription() != null ? product.getDescription() : "",
                product.getPrice(),
                product.isProductAvailable() ? "Yes" : "No",
                product.getStockQuantity()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId",  String.valueOf(product.getId()));
        metadata.put("name",       product.getName());
        metadata.put("brand",      product.getBrand() != null ? product.getBrand() : "");
        metadata.put("category",   product.getCategory() != null ? product.getCategory() : "");
        metadata.put("price",      product.getPrice() != null ? product.getPrice().toPlainString() : "0");
        metadata.put("available",  String.valueOf(product.isProductAvailable()));
        metadata.put("stock",      String.valueOf(product.getStockQuantity()));

        return new Document(String.valueOf(product.getId()), text, metadata);
    }

    private void persistSnapshot() {
        try {
            if (vectorStore instanceof SimpleVectorStore svs) {
                svs.save(new File(persistPath));
            }
        } catch (Exception e) {
            log.warn("RAG: Could not persist vector store snapshot: {}", e.getMessage());
        }
    }
}
