package com.ankush.data.service;

import com.ankush.data.entities.ItemStock;
import com.ankush.data.repository.ItemStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemStockService {
    @Autowired
    private ItemStockRepository repository;

    public List<String>getAllItemNames(){
        return repository.getAllItemStockName();
    }
    public ItemStock getItemStockByNameAndBarcode(String itemname,String barcode)
    {
        return repository.getByItemnameAndBarcode(itemname,barcode);
    }
    public int saveItemStock(ItemStock stock)
    {
        ItemStock s =getItemStockByNameAndBarcode(stock.getItemname(),stock.getBarcode());
      if(s==null)
        {
            //new
            repository.save(stock);
            return 1;
        }
        else
        {
            //update
            s.setStock(s.getStock()+stock.getStock());
            repository.save(s);
            return 2;
        }
    }
    public List<ItemStock>getAllItemStock()
    {
        return repository.findAll();
    }
    public ItemStock getItemStockByItemname(String name)
    {
        return repository.getByItemname(name);
    }
    public List<String>getItemNameByBarcode(){return repository.getItemNameByBarcode();}

}
