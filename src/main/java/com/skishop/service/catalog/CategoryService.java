package com.skishop.service.catalog;

import com.skishop.dao.category.CategoryDao;
import com.skishop.domain.product.Category;
import java.util.List;
import org.springframework.stereotype.Service;

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
