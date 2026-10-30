package com.ankush.data.service;

import com.ankush.data.entities.Item;
import com.ankush.data.entities.ItemStock;
import com.ankush.data.repository.ItemStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemStockService {
    @Autowired
    private ItemStockRepository repository;
    @Autowired @Lazy
    private ItemService itemService;

    /** Stock of the item an old line refers to, following renames and merges; null if it has no stock row. */
    public ItemStock getCurrentStock(String itemname, String barcode)
    {
        ItemStock s = getItemStockByNameAndBarcode(itemname, barcode);
        if(s != null) return s;
        s = getItemStockByItemname(itemname);
        if(s != null) return s;
        Item current = itemService.findCurrentItem(itemname, barcode);
        return current == null ? null : getItemStockByItemname(current.getItemname());
    }

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
            // Item's barcode was added or changed: keep using its existing stock row instead of starting a
            // second one (that split the stock and left rows like "blank" and "3" for the same item)
            List<ItemStock> byName = repository.getByItemname(stock.getItemname());
            if(!byName.isEmpty()) {
                s = byName.get(0);
                s.setBarcode(stock.getBarcode());
            }
        }
        if(s==null)
        {
            // line of an old bill / invoice whose item was renamed or merged since: use the item's current
            // stock row instead of starting a new row under the old name
            Item current = itemService.findCurrentItem(stock.getItemname(), stock.getBarcode());
            if(current != null && !current.getItemname().equals(stock.getItemname())) {
                List<ItemStock> rows = repository.getByItemname(current.getItemname());
                if(!rows.isEmpty()) s = rows.get(0);
            }
        }
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
    /** Stock of an item across all its stock rows (one per barcode), or null if it has none. */
    public ItemStock getItemStockByItemname(String name)
    {
        List<ItemStock> rows = repository.getByItemname(name);
        if (rows.isEmpty()) return null;
        if (rows.size() == 1) return rows.get(0);
        float total = 0;
        for (ItemStock row : rows) {
            if (row.getStock() != null) total += row.getStock();
        }
        return ItemStock.builder().itemname(name).unit(rows.get(0).getUnit()).stock(total).build();
    }
    public List<String>getItemNameByBarcode(){return repository.getItemNameByBarcode();}
    public List<ItemStock>getMinimumKgStock(){return repository.getMinimumKgStock();}
    public List<ItemStock>getMinimumNosStock(){return repository.getMinimumNosStock();}
}
