package com.lokendra.springecom.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lokendra.springecom.model.Product;
import com.lokendra.springecom.repo.ProductRepo;

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

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product getProductById(int id) {
        return productRepo.findById(id).orElse(new Product(-1));
    }

    public Product addOrUpdateProduct(Product product, MultipartFile image) throws IOException {
        product.setImageName(image.getOriginalFilename());
        product.setImageType(image.getContentType());
        product.setImageData(image.getBytes());

        return productRepo.save(product);
    }


    public void deleteProduct(int id) {
        productRepo.deleteById(id);
    }


    public List<Product> searchProducts(String keyword) {
        return productRepo.searchProducts(keyword);
    }

    public String generateDescription(String name, String category) {

        String descPrompt = String.format("""
            Write a concise and professional product description for an e-commerce listing.

            Product Name: %s
            Category: %s

            Keep it simple,engaging, and highlight its primary features or benfits.
            Avoid technical jargon and keep it customer-friendly.
            Limit description to 250 characters maximum.
            """,name,category);

        String desc = chatClient.prompt(descPrompt).call().chatResponse().getResult().getOutput().getText();

        return desc;
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

     byte[] aiImage = aiImageGenService.generateImage(imagePrompt);
    
    return aiImage;
}
}
