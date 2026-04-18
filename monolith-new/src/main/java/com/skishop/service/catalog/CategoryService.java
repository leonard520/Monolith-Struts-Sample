package com.skishop.service.catalog;

import com.skishop.dao.category.CategoryDao;
import com.skishop.domain.product.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryDao categoryDao;

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public List<Category> listAll() {
        return categoryDao.findAll();
    }
}
