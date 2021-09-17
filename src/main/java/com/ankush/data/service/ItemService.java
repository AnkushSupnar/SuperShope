package com.ankush.data.service;

import com.ankush.data.entities.Item;
import com.ankush.data.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    @Autowired
    ItemRepository repository;
    public List<Item> getAllItems(){
        return repository.findAll();
    }

    public Item getItemByName(String name)
    {
        return repository.getByItemname(name);
    }

    public Item getItemByItemnameAndBarcode(String itemname,String barcode){
            return repository.getItemByItemnameAndBarcode(itemname,barcode);
    }

    public Item getItemByBarcode(String barcode)
    {
        return repository.getItemByBarcode(barcode);
    }

    public int saveItem(Item item){
        if(item.getId()==null)
        {
            repository.save(item);
            return 1;
        }
        else
        {
            repository.save(item);
            return 2;
        }
    }

    public List<String>getAllItemNames(){
        return repository.getAllItemNames();
    }
}
