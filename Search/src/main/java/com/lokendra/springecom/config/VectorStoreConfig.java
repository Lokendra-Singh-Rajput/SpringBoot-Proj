package com.lokendra.springecom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Value("${app.vector-store.persist-path:vector-store.json}")
    private String persistPath;

    @Bean
    public VectorStore vectorStore(
            @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {

        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        File snapshot = new File(persistPath);
        if (snapshot.exists()) {
            try {
                store.load(snapshot);
                log.info("RAG: Loaded vector store from {}", snapshot.getAbsolutePath());
            } catch (Exception e) {
                log.warn("RAG: Could not load vector store snapshot — starting fresh. Reason: {}", e.getMessage());
            }
        } else {
            log.info("RAG: No existing vector store snapshot found — starting empty. " +
                     "Products will be indexed via the startup runner.");
        }

        return store;
    }
}
