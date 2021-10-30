package com.ankush.data.repository;

import com.ankush.data.entities.ItemStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock, Long> {
    @Query("select itemname from ItemStock where stock>0 ")
    List<String>getAllItemStockName();
    ItemStock getByItemnameAndBarcode(String itemname,String barcode);
    ItemStock getByItemname(String itemname);
    @Query("select itemname from ItemStock where stock>0 order by barcode")
    List<String>getItemNameByBarcode();
}