package com.ankush.data.service;

import com.ankush.data.entities.FavoriteCategoryItem;
import com.ankush.data.entities.Item;
import com.ankush.data.entities.ItemNameHistory;
import com.ankush.data.entities.ItemStock;
import com.ankush.data.repository.FavoriteCategoryItemRepository;
import com.ankush.data.repository.ItemNameHistoryRepository;
import com.ankush.data.repository.ItemRepository;
import com.ankush.data.repository.ItemStockRepository;
import com.ankush.data.repository.PurchaseTransactionRepository;
import com.ankush.data.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    ItemRepository repository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ItemStockRepository itemStockRepository;
    @Autowired
    ItemNameHistoryRepository nameHistoryRepository;
    @Autowired
    FavoriteCategoryItemRepository favoriteCategoryItemRepository;
    @Autowired
    PurchaseTransactionRepository purchaseTransactionRepository;
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

    /**
     * Item for a barcode, preferring active items when several share it. A barcode of an item that was
     * merged into another brings up that other item.
     */
    public Item getItemByBarcode(String barcode)
    {
        return resolveReplacement(findByBarcodeRaw(barcode));
    }

    private Item findByBarcodeRaw(String barcode) {
        List<Item> items = repository.getByBarcode(barcode);
        return items.stream().filter(Item::isActiveItem).findFirst()
                .orElse(items.isEmpty() ? null : items.get(0));
    }

    /**
     * The item an old bill / invoice line refers to today, following renames and merges: by name and barcode,
     * by name, by an old name in the history, then by barcode. Null if it can't be found.
     */
    public Item findCurrentItem(String itemname, String barcode) {
        String name = itemname == null ? "" : itemname.trim();
        String code = barcode == null ? "" : barcode.trim();
        Item found = name.isEmpty() ? null : repository.getItemByItemnameAndBarcode(name, code);
        if (found == null && !name.isEmpty()) found = repository.getByItemname(name);
        if (found == null && !name.isEmpty()) {
            String current = getOldNameMap().get(name);
            if (current != null) found = repository.getByItemname(current);
        }
        if (found == null && !code.isEmpty()) found = findByBarcodeRaw(code);
        return resolveReplacement(found);
    }

    /** Follows "merged into" links to the item that replaced this one (the item itself if none). */
    public Item resolveReplacement(Item item) {
        for (int hops = 0; item != null && item.getReplacedBy() != null && hops < 10; hops++) {
            Item next = repository.findById(item.getReplacedBy()).orElse(null);
            if (next == null) break;
            item = next;
        }
        return item;
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

    public List<String> getActiveItemNames() {
        return repository.getActiveItemNames();
    }

    public List<Item> getActiveItems() {
        return repository.getActiveItems();
    }

    public List<String> getCategories() {
        return repository.getCategories();
    }

    public Item getItemByShortCode(String shortCode) {
        if (shortCode == null || shortCode.isBlank()) return null;
        return resolveReplacement(repository.findFirstByShortCode(shortCode.trim()));
    }

    public boolean isShortCodeTaken(String shortCode, Long excludeItemId) {
        // the item that holds the code, not the one it resolves to: a merged item keeps its code
        Item other = shortCode == null || shortCode.isBlank() ? null : repository.findFirstByShortCode(shortCode.trim());
        return other != null && !other.getId().equals(excludeItemId);
    }

    /**
     * Extra searchable text per item name (English search name + short code), lower-cased.
     * Used by the autocomplete so an item shown in Marathi can be found by "sugar" or "101".
     */
    public Map<String, String> getSearchKeys() {
        Map<String, String> keys = new HashMap<>();
        for (Item item : repository.findAll()) {
            if (item.getItemname() == null) continue;
            StringBuilder sb = new StringBuilder();
            if (item.getSearchName() != null) sb.append(item.getSearchName()).append(' ');
            if (item.getShortCode() != null) sb.append(item.getShortCode());
            if (sb.length() > 0) {
                keys.merge(item.getItemname(), sb.toString().toLowerCase(), (a, b) -> a + " " + b);
            }
        }
        // old names (before a rename / merge) still find the item
        getOldNameMap().forEach((oldName, current) ->
                keys.merge(current, oldName.toLowerCase(), (a, b) -> a + " " + b));
        return keys;
    }

    /** Old item name -> the item's current name, from renames and merges. */
    public Map<String, String> getOldNameMap() {
        Map<String, String> map = new HashMap<>();
        for (ItemNameHistory h : nameHistoryRepository.findAll()) {
            if (h.getItem() != null && h.getOldName() != null && !h.getOldName().equals(h.getItem().getItemname())) {
                map.put(h.getOldName(), h.getItem().getItemname());
            }
        }
        return map;
    }

    /**
     * Number of bill lines per item name over the last {@code days} days. Sales under an old name count
     * for the item's current name, so a renamed or merged item keeps its best-seller rank.
     */
    public Map<String, Long> getSaleCounts(int days) {
        Map<String, String> oldNames = getOldNameMap();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : transactionRepository.getSaleCountSince(LocalDate.now().minusDays(days))) {
            if (row[0] == null) continue;
            String name = oldNames.getOrDefault((String) row[0], (String) row[0]);
            counts.merge(name, (Long) row[1], Long::sum);
        }
        return counts;
    }

    /** Item names sold most often in the last {@code days} days, best first. */
    public List<String> getTopSellingNames(int days, int limit) {
        Set<String> active = new HashSet<>(getActiveItemNames());
        return getSaleCounts(days).entrySet().stream()
                .filter(e -> active.contains(e.getKey()))
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /** Existing item names that look like {@code name} (shared prefix or one contains the other). */
    public List<String> findSimilarNames(String name) {
        String n = normalize(name);
        if (n.isEmpty()) return new ArrayList<>();
        String prefix = n.substring(0, Math.min(3, n.length()));
        return getAllItemNames().stream()
                .filter(other -> {
                    String o = normalize(other);
                    return !o.isEmpty() && (o.contains(n) || n.contains(o) || o.startsWith(prefix));
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[\\s\\d.]", "");
    }

    /**
     * Gives the next free numeric short code (starting at 101) to every active item that has none,
     * best-selling items first so the busiest items get the shortest codes.
     * @return number of items updated
     */
    public int assignMissingShortCodes() {
        // barcodes count as used too: billing looks up the barcode first, so code "110" must not be another item's barcode
        Set<String> used = repository.findAll().stream()
                .flatMap(i -> java.util.stream.Stream.of(i.getShortCode(), i.getBarcode()))
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());
        Map<String, Long> sales = getSaleCounts(365);
        List<Item> pending = getActiveItems().stream()
                .filter(i -> i.getShortCode() == null || i.getShortCode().isBlank())
                .sorted(Comparator.comparingLong((Item i) -> sales.getOrDefault(i.getItemname(), 0L)).reversed())
                .collect(Collectors.toList());
        int next = 101;
        for (Item item : pending) {
            while (used.contains(String.valueOf(next))) next++;
            item.setShortCode(String.valueOf(next));
            used.add(String.valueOf(next));
            repository.save(item);
        }
        return pending.size();
    }

    /**
     * Active items neither sold nor purchased in the last {@code days} days. Stock is not looked at: the
     * shop bills without entering every purchase, so stock figures are not reliable.
     */
    public List<Item> findUnusedItems(int days) {
        Map<String, Long> sales = getSaleCounts(days);
        Map<String, String> oldNames = getOldNameMap();
        Set<String> purchased = new HashSet<>();
        for (String name : purchaseTransactionRepository.getPurchasedNamesSince(LocalDate.now().minusDays(days))) {
            purchased.add(oldNames.getOrDefault(name, name));
        }
        return getActiveItems().stream()
                .filter(i -> !sales.containsKey(i.getItemname()) && !purchased.contains(i.getItemname()))
                .collect(Collectors.toList());
    }

    public void deactivate(List<Item> items) {
        for (Item item : items) {
            item.setActive(false);
        }
        repository.saveAll(items);
    }

    /**
     * Changes an existing item in place (e.g. the company changed "10 gram" to "9 gram") instead of creating a
     * new item. Old bills and purchases keep the name they were made with; the old name is remembered so the
     * item is still found by it, and its stock rows follow the new name.
     * @throws IllegalArgumentException when the new name or barcode already belongs to another item
     */
    @Transactional
    public Item renameItem(Item item, String newName, String barcode, String unit,
                           float purchasePrice, float mrp, float sellingPrice) {
        String name = newName == null ? "" : newName.trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Enter the item name");
        Item sameName = repository.getByItemname(name);
        if (sameName != null && !sameName.getId().equals(item.getId())) {
            throw new IllegalArgumentException("Another item already has this name. Use MERGE in Item Master to join them.");
        }
        String code = barcode == null ? "" : barcode.trim();
        if (!code.isEmpty()) {
            Item sameBarcode = findByBarcodeRaw(code);
            if (sameBarcode != null && !sameBarcode.getId().equals(item.getId()) && sameBarcode.isActiveItem()) {
                throw new IllegalArgumentException("This barcode belongs to another item");
            }
        }
        String oldName = item.getItemname();
        if (!name.equals(oldName)) {
            for (ItemStock stock : itemStockRepository.getByItemname(oldName)) {
                stock.setItemname(name);
                itemStockRepository.save(stock);
            }
            nameHistoryRepository.save(ItemNameHistory.builder().item(item).oldName(oldName)
                    .reason(ItemNameHistory.RENAMED).changedOn(LocalDate.now()).build());
        }
        item.setItemname(name);
        item.setBarcode(code);
        item.setUnit(unit);
        item.setPrice(purchasePrice);
        item.setRate(mrp);
        item.setSailingprice(sellingPrice);
        item.setActive(true);
        return repository.save(item);
    }

    /**
     * Joins duplicate items into {@code keep}: their stock is added to it, favourites point to it, their names
     * become old names of it, and they are made inactive with their barcode / short code bringing up {@code keep}.
     * Nothing is deleted, old bills still show the names they were made with.
     * @return number of items merged
     */
    @Transactional
    public int mergeItems(Item keep, List<Item> duplicates) {
        int merged = 0;
        for (Item dup : duplicates) {
            if (dup.getId().equals(keep.getId())) continue;
            moveStock(dup, keep);
            for (FavoriteCategoryItem fav : favoriteCategoryItemRepository.findByItemId(dup.getId())) {
                if (favoriteCategoryItemRepository.findByCategoryIdAndItemId(fav.getCategory().getId(), keep.getId()) != null) {
                    favoriteCategoryItemRepository.delete(fav);
                } else {
                    fav.setItem(keep);
                    favoriteCategoryItemRepository.save(fav);
                }
            }
            for (ItemNameHistory h : nameHistoryRepository.findByItemId(dup.getId())) {
                h.setItem(keep);
                nameHistoryRepository.save(h);
            }
            nameHistoryRepository.save(ItemNameHistory.builder().item(keep).oldName(dup.getItemname())
                    .reason(ItemNameHistory.MERGED).changedOn(LocalDate.now()).build());
            if ((keep.getBarcode() == null || keep.getBarcode().isBlank()) && dup.getBarcode() != null && !dup.getBarcode().isBlank()) {
                keep.setBarcode(dup.getBarcode());
            }
            dup.setActive(false);
            dup.setReplacedBy(keep.getId());
            repository.save(dup);
            merged++;
        }
        keep.setActive(true);
        repository.save(keep);
        return merged;
    }

    private void moveStock(Item from, Item to) {
        float moved = 0;
        for (ItemStock stock : itemStockRepository.getByItemname(from.getItemname())) {
            if (stock.getStock() != null) moved += stock.getStock();
            itemStockRepository.delete(stock);
        }
        if (moved == 0) return;
        List<ItemStock> target = itemStockRepository.getByItemname(to.getItemname());
        if (target.isEmpty()) {
            itemStockRepository.save(ItemStock.builder().itemname(to.getItemname()).barcode(to.getBarcode())
                    .unit(to.getUnit()).stock(moved).build());
        } else {
            ItemStock row = target.get(0);
            row.setStock((row.getStock() == null ? 0 : row.getStock()) + moved);
            itemStockRepository.save(row);
        }
    }
}
