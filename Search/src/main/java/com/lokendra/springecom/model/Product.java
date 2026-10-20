package com.lokendra.springecom.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private String brand;
    private BigDecimal price;
    private String category;
    private Date releaseDate;
    private boolean productAvailable;
    private int stockQuantity;

    
    
    private String imageName;

    
    private String imageType;

    
    private String imageStoredName;

    public Product() {
    }

    public Product(int id) {
        this.id = id;
    }

    public Product(int id, String name, String description, String brand, BigDecimal price,
                   String category, Date releaseDate, boolean productAvailable, int stockQuantity,
                   String imageName, String imageType, String imageStoredName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.price = price;
        this.category = category;
        this.releaseDate = releaseDate;
        this.productAvailable = productAvailable;
        this.stockQuantity = stockQuantity;
        this.imageName = imageName;
        this.imageType = imageType;
        this.imageStoredName = imageStoredName;
    }
}
