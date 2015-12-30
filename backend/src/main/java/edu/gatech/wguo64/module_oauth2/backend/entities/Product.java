package edu.gatech.wguo64.module_oauth2.backend.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by guoweidong on 12/29/15.
 */
@Entity
public class Product {
    @Id
    private Long id;
    private String name;
    private double price;
    //Constructors
    public Product() {}
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Setters and getters
    public void setName(String name) {
        this.name = name;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
}
