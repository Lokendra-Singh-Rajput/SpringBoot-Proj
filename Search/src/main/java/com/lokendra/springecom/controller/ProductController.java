package com.lokendra.springecom.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.service.AiImageGeneratorService;
import com.lokendra.springecom.service.ProductService;

import jakarta.annotation.Nullable;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AiImageGeneratorService aiImageGenService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {

        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    @GetMapping("/product/{productId}/image")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable int productId) {

        Product product = productService.getProductById(productId);

        if (product == null || product.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        try {
            mediaType = MediaType.parseMediaType(product.getImageType());
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + product.getImageName() + "\"")
                .contentType(mediaType)
                .body(product.getImageData());
    }

    @PostMapping("/product/generate-description")
    public ResponseEntity<String> generateDescription(
            @RequestParam String name,
            @RequestParam String category) {

        try {
            String aiDesc = productService.generateDescription(name, category);
            return ResponseEntity.ok(aiDesc);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/product/generate-image")
    public ResponseEntity<?> generateImage(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam String description) {

        try {
            byte[] image =
                    productService.generateImage(name, category, description);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(image);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/product")
    public ResponseEntity<?> addProduct(
            @RequestPart Product product,
            @RequestPart MultipartFile imageFile) {

        try {
            Product saved =
                    productService.addOrUpdateProduct(product, imageFile);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable int id,
            @RequestPart Product product,
            @RequestPart(required = false) @Nullable MultipartFile imageFile) {

        try {

            product.setId(id);

            Product updated =
                    productService.addOrUpdateProduct(product, imageFile);

            return ResponseEntity.ok(updated);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {

        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        productService.deleteProduct(id);

        return ResponseEntity.ok("Deleted Successfully");
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String keyword) {

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