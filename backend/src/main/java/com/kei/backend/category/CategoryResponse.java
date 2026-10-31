/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kei.backend.category;

/**
 *
 * @author LENOVO
 */
public record CategoryResponse(Long id, String name, CategoryType type, String icon) {
    
    public static CategoryResponse from(Category c){
        return new CategoryResponse(c.getId(), c.getName(), c.getType(), c.getIcon());
    }
    
}
