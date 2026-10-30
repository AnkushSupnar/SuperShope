package com.ankush.data.repository;

import com.ankush.data.entities.ItemNameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemNameHistoryRepository extends JpaRepository<ItemNameHistory, Long> {
    List<ItemNameHistory> findByItemId(Long itemId);
}
