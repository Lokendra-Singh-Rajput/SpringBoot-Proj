package com.lokendra.springecom.controller;

import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.service.AiImageGeneratorService;
import com.lokendra.springecom.service.ImageStorageService;
import com.lokendra.springecom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:5173}")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AiImageGeneratorService aiImageGenService;

    @Autowired
    private ImageStorageService imageStorageService;

    
    @GetMapping("/products")
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sort) {

        
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
            Page<Product> productPage = productService.getAllProductsPaged(pageable);
            return ResponseEntity.ok(productPage);
        }

        return ResponseEntity.ok(productService.getAllProducts());
    }

    
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        
        return ResponseEntity.ok(productService.getProductById(id));
    }

    
    @GetMapping("/product/{productId}/image")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable int productId)
            throws IOException {

        Product product = productService.getProductById(productId);

        if (product.getImageStoredName() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = imageStorageService.load(product.getImageStoredName());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            mediaType = MediaType.parseMediaType(product.getImageType());
        } catch (Exception ignored) { }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + product.getImageName() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(mediaType)
                .body(imageBytes);
    }

    
    @PostMapping("/product/generate-description")
    public ResponseEntity<String> generateDescription(
            @RequestParam String name,
            @RequestParam String category) {

        String aiDesc = productService.generateDescription(name, category);
        return ResponseEntity.ok(aiDesc);
    }

    
    @PostMapping("/product/generate-image")
    public ResponseEntity<byte[]> generateImage(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam String description) {

        byte[] image = productService.generateImage(name, category, description);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    
    @PostMapping("/product/generate-product")
    public ResponseEntity<Map<String, Object>> generateProduct(@RequestParam String query) {
        try {
            String jsonResponse = productService.generateProduct(query);

            
            String cleaned = jsonResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\s*", "").replaceAll("\\s*```$", "").trim();
            }

            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> productData = mapper.readValue(cleaned, Map.class);
            return ResponseEntity.ok(productData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate product: " + e.getMessage()));
        }
    }

    
    @PostMapping("/product")
    public ResponseEntity<Product> addProduct(
            @RequestPart Product product,
            @RequestPart MultipartFile imageFile) throws IOException {

        Product saved = productService.addOrUpdateProduct(product, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    
    @PutMapping("/product/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable int id,
            @RequestPart Product product,
            @RequestPart(required = false) MultipartFile imageFile) throws IOException {

        product.setId(id);

        
        if (imageFile == null || imageFile.isEmpty()) {
            Product existing = productService.getProductById(id);
            product.setImageName(existing.getImageName());
            product.setImageType(existing.getImageType());
            product.setImageStoredName(existing.getImageStoredName());
        }

        Product updated = productService.addOrUpdateProduct(product, imageFile);
        return ResponseEntity.ok(updated);
    }

    
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id); 
        return ResponseEntity.ok("Deleted Successfully");
    }

    
    @GetMapping("/products/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sort) {

        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
            return ResponseEntity.ok(productService.searchProductsPaged(keyword, pageable));
        }

        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    
    @GetMapping("/test-gemini")
    public ResponseEntity<String> testGemini() {
        return ResponseEntity.ok(aiImageGenService.testConnection());
    }

    @GetMapping("/test-image")
    public ResponseEntity<byte[]> testImage() {
        byte[] image = aiImageGenService.generateImage(
                "Generate a realistic red sports shoe on a white background.");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }
}