package com.ankush.data.repository;

import com.ankush.data.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select itemname from Item")
    List<String> getAllItemNames();

    Item getByItemname(String name);
    Item getItemByItemnameAndBarcode(String itemname,String barcode);
    Item getItemByBarcode(String barcode);
}