package com.mateusz.library.controllers;

import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.services.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/category")
public class CategoryControler {

    private final CategoryService categoryService;
    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/deleteById/{categoryId}")
    public ResponseEntity<CategoryEntity> deleteCategoryById(@PathVariable(name = "categoryId") Long categoryId) {
        CategoryEntity deletedCategory = categoryService.deleteCategoryById(categoryId);
        return new ResponseEntity<>(deletedCategory, HttpStatus.OK);
    }
    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/deleteByName/{categoryName}")
    public ResponseEntity<CategoryEntity> deleteCategoryByName(@PathVariable(name = "categoryName") String categoryName) {
        CategoryEntity deletedCategory = categoryService.deleteCategoryByName(categoryName);
        return new ResponseEntity<>(deletedCategory,HttpStatus.OK);
    }
}
