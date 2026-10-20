package com.lokendra.springecom.config;

import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.repo.ProductRepo;
import com.lokendra.springecom.service.ProductVectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class ProductVectorInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductVectorInitializer.class);

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductVectorService productVectorService;

    @Value("${app.vector-store.persist-path:vector-store.json}")
    private String persistPath;

    @Override
    public void run(ApplicationArguments args) {
        try {
            File snapshot = new File(persistPath);
            if (!snapshot.exists()) {
                List<Product> products = productRepo.findAll();
                if (!products.isEmpty()) {
                    log.info("RAG: Indexing {} existing products into vector store...", products.size());
                    productVectorService.indexAll(products);
                    log.info("RAG: Initial vector store setup complete.");
                } else {
                    log.info("RAG: No products in database to index.");
                }
            } else {
                log.info("RAG: Existing vector store snapshot found; skipping initial bulk index.");
            }
        } catch (Exception e) {
            log.warn("RAG: Initial vector store indexing encountered an issue (is Ollama running?): {}", e.getMessage());
        }
    }
}
