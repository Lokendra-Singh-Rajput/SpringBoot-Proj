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

    @org.springframework.beans.factory.annotation.Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

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
        String cleanName = (name != null && !name.isBlank()) ? name.trim() : "Product";
        String cleanCat = (category != null && !category.isBlank()) ? category.trim() : "Electronics";

        if (isOllamaReachable()) {
            try {
                String descPrompt = String.format("""
                    Write a concise and professional product description for an e-commerce listing.
                    Product Name: %s
                    Category: %s
                    Keep it simple, engaging, and highlight its primary features or benefits.
                    Avoid technical jargon and keep it customer-friendly.
                    Limit description to 250 characters maximum.
                    """, cleanName, cleanCat);

                return chatClient.prompt(descPrompt)
                        .call()
                        .chatResponse()
                        .getResult()
                        .getOutput()
                        .getText()
                        .trim();
            } catch (Exception ignored) {
            }
        }

        return buildSmartDescription(cleanName, cleanCat);
    }

    private String buildSmartDescription(String name, String category) {
        String cat = category.toLowerCase();
        if (cat.contains("laptop")) {
            return String.format("High-performance %s engineered for power and portability. Features a stunning display, all-day battery life, ultra-responsive multitasking, and a lightweight durable chassis.", name);
        } else if (cat.contains("headphone") || cat.contains("audio")) {
            return String.format("Experience immersive studio-grade sound with %s. Features advanced noise cancellation, custom-tuned acoustic drivers, ergonomic cushions, and extended playtime.", name);
        } else if (cat.contains("mobile") || cat.contains("phone")) {
            return String.format("Next-generation %s with brilliant OLED display, pro-level multi-lens camera system with night mode, ultra-fast 5G connectivity, and all-day intelligent battery life.", name);
        } else if (cat.contains("watch")) {
            return String.format("Sophisticated %s designed for modern lifestyles. Includes precision biometric tracking, crystal-clear AMOLED touchscreen, water-resistant build, and seamless connectivity.", name);
        } else if (cat.contains("electronic")) {
            return String.format("Cutting-edge %s built with premium materials for maximum durability and peak efficiency. Delivers reliable performance for work, home, and entertainment.", name);
        } else if (cat.contains("fashion") || cat.contains("cloth") || cat.contains("shoe")) {
            return String.format("Elevate your everyday style with %s. Tailored from premium breathable materials for superior comfort, modern aesthetic, and all-day wearability.", name);
        } else if (cat.contains("toy") || cat.contains("game")) {
            return String.format("Engaging and fun %s crafted from child-safe, durable materials. Promotes creative learning, imaginative play, and hours of entertainment.", name);
        } else if (cat.contains("home")) {
            return String.format("Transform your living space with %s. Built with modern craftsmanship to combine elegant design with practical daily utility.", name);
        }
        return String.format("Premium quality %s designed for exceptional reliability and modern performance. Verified authentic and ready for fast delivery.", name);
    }

    public byte[] generateImage(String name, String category, String description) {
        try {
            if (aiImageGenService != null) {
                String imagePrompt = String.format("Professional studio photo of %s (%s category), centered, clean background", name, category);
                byte[] generated = aiImageGenService.generateImage(imagePrompt);
                if (generated != null && generated.length > 0) {
                    return generated;
                }
            }
        } catch (Exception ignored) {
        }
        return generateBrandedPlaceholderImage(name, category);
    }

    private byte[] generateBrandedPlaceholderImage(String name, String category) {
        int width = 800;
        int height = 600;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        java.awt.GradientPaint bg = new java.awt.GradientPaint(0, 0, new java.awt.Color(21, 18, 16), width, height, new java.awt.Color(42, 34, 29));
        g2.setPaint(bg);
        g2.fillRect(0, 0, width, height);

        g2.setColor(new java.awt.Color(196, 91, 44, 40));
        g2.fillOval(width - 250, -50, 300, 300);
        g2.fillOval(-80, height - 220, 260, 260);

        g2.setColor(new java.awt.Color(255, 255, 255, 25));
        g2.drawRoundRect(30, 30, width - 60, height - 60, 24, 24);

        g2.setColor(new java.awt.Color(196, 91, 44));
        g2.fillRoundRect(50, 50, 160, 34, 12, 12);
        g2.setColor(java.awt.Color.WHITE);
        g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        g2.drawString("LOKENDRA STORE", 65, 72);

        String catDisplay = (category != null && !category.isBlank()) ? category.toUpperCase() : "PRODUCT";
        g2.setColor(new java.awt.Color(224, 122, 63));
        g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        g2.drawString(catDisplay, 50, 240);

        g2.setColor(new java.awt.Color(247, 244, 238));
        g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 32));
        String title = (name != null && !name.isBlank()) ? name : "Verified Product";
        if (title.length() > 30) {
            title = title.substring(0, 28) + "...";
        }
        g2.drawString(title, 50, 290);

        g2.setColor(new java.awt.Color(180, 170, 160));
        g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 15));
        g2.drawString("Official Certified Product Listing • 2026 Edition", 50, 330);

        g2.dispose();

        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (java.io.IOException e) {
            return new byte[0];
        }
    }

    public String generateProduct(String query) {
        if (isOllamaReachable()) {
            try {
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
                    """, query);

                return chatClient.prompt(prompt)
                        .call()
                        .chatResponse()
                        .getResult()
                        .getOutput()
                        .getText();
            } catch (Exception ignored) {
            }
        }
        return buildSmartProductJson(query);
    }

    private String buildSmartProductJson(String query) {
        String q = query != null ? query.trim() : "Product";
        String lower = q.toLowerCase();
        String cat = "Electronics";
        String brand = "Generic";
        int price = 2499;

        if (lower.contains("laptop") || lower.contains("macbook") || lower.contains("notebook")) {
            cat = "Laptop";
            brand = lower.contains("apple") ? "Apple" : (lower.contains("dell") ? "Dell" : (lower.contains("lenovo") ? "Lenovo" : "ASUS"));
            price = 69999;
        } else if (lower.contains("headphone") || lower.contains("earbud") || lower.contains("airpod")) {
            cat = "Headphone";
            brand = lower.contains("sony") ? "Sony" : (lower.contains("bose") ? "Bose" : "Boat");
            price = 9999;
        } else if (lower.contains("phone") || lower.contains("mobile") || lower.contains("iphone") || lower.contains("galaxy")) {
            cat = "Mobile";
            brand = lower.contains("apple") ? "Apple" : (lower.contains("samsung") ? "Samsung" : "OnePlus");
            price = 44999;
        } else if (lower.contains("watch")) {
            cat = "Electronics";
            brand = "Titan";
            price = 5999;
        } else if (lower.contains("shoe") || lower.contains("shirt") || lower.contains("jacket")) {
            cat = "Fashion";
            brand = "Nike";
            price = 3999;
        }

        String today = java.time.LocalDate.now().toString();
        String desc = buildSmartDescription(q, cat);
        return String.format(
                "{\"name\":\"%s\",\"brand\":\"%s\",\"description\":\"%s\",\"price\":%d,\"category\":\"%s\",\"stockQuantity\":25,\"releaseDate\":\"%s\",\"productAvailable\":true}",
                q.replace("\"", "\\\""), brand, desc.replace("\"", "\\\""), price, cat, today
        );
    }
}
