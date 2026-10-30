package com.ankush.data.repository;

import com.ankush.data.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select itemname from Item order by itemname")
    List<String> getAllItemNames();

    Item getByItemname(String name);
    Item getItemByItemnameAndBarcode(String itemname,String barcode);
    // list, not single: client data has the same barcode on more than one item
    @Query("from Item where barcode=:barcode order by id")
    List<Item> getByBarcode(@Param("barcode") String barcode);

    Item findFirstByShortCode(String shortCode);

    @Query("select itemname from Item where active is null or active = true order by itemname")
    List<String> getActiveItemNames();

    @Query("from Item where active is null or active = true order by itemname")
    List<Item> getActiveItems();

    @Query("select distinct category from Item where category is not null and category <> '' order by category")
    List<String> getCategories();

    // [no selling price, no MRP, no short code] among active items, for the dashboard "needs attention" card
    @Query("select sum(case when sailingprice <= 0 then 1 else 0 end), sum(case when rate <= 0 then 1 else 0 end), "
            + "sum(case when shortCode is null or shortCode = '' then 1 else 0 end), count(id) "
            + "from Item where active is null or active = true")
    List<Object[]> getActiveItemGaps();
}