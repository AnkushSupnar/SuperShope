package com.ankush.data.service;

import com.ankush.data.entities.FavoriteCategory;
import com.ankush.data.entities.FavoriteCategoryItem;
import com.ankush.data.entities.Item;
import com.ankush.data.repository.FavoriteCategoryItemRepository;
import com.ankush.data.repository.FavoriteCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class FavoriteCategoryService {
    @Autowired
    private FavoriteCategoryRepository categoryRepository;

    @Autowired
    private FavoriteCategoryItemRepository categoryItemRepository;

    public List<FavoriteCategory> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    public int saveCategory(FavoriteCategory category) {
        if (category.getId() == null) {
            categoryRepository.save(category);
            return 1;
        } else {
            categoryRepository.save(category);
            return 2;
        }
    }

    public FavoriteCategory getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public FavoriteCategory getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public List<String> getItemNamesByCategory(Long categoryId) {
        return categoryItemRepository.getItemNamesByCategoryId(categoryId);
    }

    public int addItemToCategory(FavoriteCategory category, Item item) {
        FavoriteCategoryItem existing = categoryItemRepository.findByCategoryIdAndItemId(category.getId(), item.getId());
        if (existing != null) {
            return 0;
        }
        FavoriteCategoryItem categoryItem = FavoriteCategoryItem.builder()
                .category(category)
                .item(item)
                .build();
        categoryItemRepository.save(categoryItem);
        return 1;
    }

    @Transactional
    public void removeItemFromCategory(Long categoryId, Long itemId) {
        categoryItemRepository.deleteByCategoryIdAndItemId(categoryId, itemId);
    }
}
