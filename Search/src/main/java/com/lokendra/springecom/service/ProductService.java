package com.lokendra.springecom.service;

import com.lokendra.springecom.exception.ResourceNotFoundException;
import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.repo.ProductRepo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AiImageGeneratorService aiImageGenService;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private ProductVectorService productVectorService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    

    
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    
    public Page<Product> getAllProductsPaged(Pageable pageable) {
        return productRepo.findAll(pageable);
    }

    
    public Product getProductById(int id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    

    
    public Product addOrUpdateProduct(Product product, MultipartFile imageFile) throws IOException {

        if (imageFile != null && !imageFile.isEmpty()) {
            
            if (product.getImageStoredName() != null) {
                imageStorageService.delete(product.getImageStoredName());
            }
            String storedName = imageStorageService.store(imageFile);
            product.setImageName(imageFile.getOriginalFilename());
            product.setImageType(imageFile.getContentType());
            product.setImageStoredName(storedName);
        }

        Product saved = productRepo.save(product);
        productVectorService.indexProduct(saved);
        return saved;
    }

    
    public Product saveWithAiImage(Product product, byte[] imageBytes, String mimeType)
            throws IOException {
        
        String storedName = imageStorageService.storeBytes(imageBytes, mimeType);
        product.setImageName("ai-generated.png");
        product.setImageType(mimeType);
        product.setImageStoredName(storedName);
        Product saved = productRepo.save(product);
        productVectorService.indexProduct(saved);
        return saved;
    }

    @Transactional
    public void deleteProduct(int id) {
        Product product = getProductById(id); 
        imageStorageService.delete(product.getImageStoredName());
        productVectorService.removeProduct(id);

        
        jdbcTemplate.update("UPDATE order_item SET product_id = NULL WHERE product_id = ?", id);

        productRepo.deleteById(id);
    }

    

    public List<Product> searchProducts(String keyword) {
        return productRepo.searchProducts(keyword);
    }

    public Page<Product> searchProductsPaged(String keyword, Pageable pageable) {
        return productRepo.searchProducts(keyword, pageable);
    }

    

    public String generateDescription(String name, String category) {

        String descPrompt = String.format("""
            Write a concise and professional product description for an e-commerce listing.

            Product Name: %s
            Category: %s

            Keep it simple, engaging, and highlight its primary features or benefits.
            Avoid technical jargon and keep it customer-friendly.
            Limit description to 250 characters maximum.
            """, name, category);

        return chatClient.prompt(descPrompt)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }

    public byte[] generateImage(String name, String category, String description) {

        String imagePrompt = String.format("""
            Generate a high-quality, realistic product image for an e-commerce website.

            Product Name: %s
            Category: %s
            Description: %s

            Requirements:
            - Show only the product on a clean white background.
            - Use professional studio lighting.
            - Make the product centered and fully visible.
            - Ensure the image is realistic and visually appealing.
            - No people, hands, text, logos, watermarks, or extra objects.
            - Suitable for an online shopping catalog.
            - High resolution with sharp details.
            """, name, category, description);

        return aiImageGenService.generateImage(imagePrompt);
    }

    
    public String generateProduct(String query) {

        String prompt = String.format("""
            You are a product catalog assistant for an Indian e-commerce store.
            A user wants to add a new product. Based on their description, generate realistic product details.

            User description: "%s"

            Respond ONLY with a valid JSON object (no markdown, no explanation) with exactly these fields:
            {
              "name": "...",
              "brand": "...",
              "description": "...",
              "price": <number in INR, no currency symbol>,
              "category": "<one of: Laptop, Headphone, Mobile, Electronics, Toys, Fashion, Books, Sports, Home>",
              "stockQuantity": <number>,
              "releaseDate": "YYYY-MM-DD",
              "productAvailable": true
            }

            Rules:
            - name: concise product title (max 60 chars)
            - brand: realistic brand name that makes products in this category
            - description: 2-3 engaging sentences, max 300 chars
            - price: realistic Indian market price as a plain number (e.g. 49999)
            - category: must be exactly one of the listed values
            - stockQuantity: between 10 and 500
            - releaseDate: today's date or a recent realistic date
            - productAvailable: always true
            """, query);

        return chatClient.prompt(prompt)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }
}
