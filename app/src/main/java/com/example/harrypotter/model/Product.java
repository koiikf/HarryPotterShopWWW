package com.example.harrypotter.model;

public class Product {
    public int product_id;
    public String title;
    public String description;
    public String photoPath;
    public int price;

    public Product(int note_id, String title, String description, String photoPath, int price) {
        this.product_id = note_id;
        this.title = title;
        this.description = description;
        this.photoPath = photoPath;
        this.price = price;
    }
}
