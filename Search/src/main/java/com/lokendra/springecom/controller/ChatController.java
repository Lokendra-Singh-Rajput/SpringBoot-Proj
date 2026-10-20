package com.lokendra.springecom.controller;

import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:5173}")
public class ChatController {

    @Autowired
    private RagService ragService;

    
    @PostMapping("/chat/assistant")
    public ResponseEntity<com.lokendra.springecom.model.dto.AgentResponse> chatWithAssistant(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "").trim();
        if (message.isEmpty()) {
            return ResponseEntity.badRequest().body(new com.lokendra.springecom.model.dto.AgentResponse(
                    "Please provide a valid question.",
                    "No user query provided.",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            ));
        }

        com.lokendra.springecom.model.dto.AgentResponse response = ragService.chatWithAgent(message);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/products/semantic-search")
    public ResponseEntity<List<Product>> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {

        List<Product> products = ragService.semanticSearch(query, topK);
        return ResponseEntity.ok(products);
    }

    @Autowired
    private com.lokendra.springecom.repo.ProductRepo productRepo;

    @Autowired
    private com.lokendra.springecom.service.ProductVectorService productVectorService;

    
    @PostMapping("/chat/reindex")
    public ResponseEntity<Map<String, Object>> reindexAllProducts() {
        List<Product> products = productRepo.findAll();
        productVectorService.indexAll(products);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "indexedCount", products.size()));
    }
}
