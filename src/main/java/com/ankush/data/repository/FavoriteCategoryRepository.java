package com.ankush.data.repository;

import com.ankush.data.entities.FavoriteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteCategoryRepository extends JpaRepository<FavoriteCategory, Long> {
    List<FavoriteCategory> findAllByOrderByDisplayOrderAsc();

    FavoriteCategory findByName(String name);

    @Query("select c.name from FavoriteCategory c order by c.displayOrder")
    List<String> getAllCategoryNames();
}
