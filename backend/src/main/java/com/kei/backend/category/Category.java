/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kei.backend.category;

import jakarta.persistence.*;

/**
 *
 * @author LENOVO
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;

    @Column(length = 10)
    private String icon;

    protected Category() {
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public String getIcon() { return icon; }
}