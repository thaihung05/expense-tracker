/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kei.backend.category;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author LENOVO
 */
@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepo;
    
    public CategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }
    
    public List<CategoryResponse> getAll(){
        return categoryRepo.findAll().stream().map(CategoryResponse::from).toList();
    }       
}
