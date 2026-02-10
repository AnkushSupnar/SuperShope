package com.ankush.data.repository;

import com.ankush.data.entities.FavoriteCategoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteCategoryItemRepository extends JpaRepository<FavoriteCategoryItem, Long> {
    List<FavoriteCategoryItem> findByCategoryId(Long categoryId);

    FavoriteCategoryItem findByCategoryIdAndItemId(Long categoryId, Long itemId);

    @Query("select ci.item.itemname from FavoriteCategoryItem ci where ci.category.id = :categoryId order by ci.item.itemname")
    List<String> getItemNamesByCategoryId(@Param("categoryId") Long categoryId);

    void deleteByCategoryIdAndItemId(Long categoryId, Long itemId);
}
