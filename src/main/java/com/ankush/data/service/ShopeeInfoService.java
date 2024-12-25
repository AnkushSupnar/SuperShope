package com.ankush.data.service;
import com.ankush.data.entities.ShopeeInfo;
import com.ankush.data.repository.ShopeeInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShopeeInfoService {

    @Autowired
    private ShopeeInfoRepository shopeeInfoRepository;

    // Save ShopeeInfo details
    public ShopeeInfo saveShopeeInfo(ShopeeInfo shopeeInfo) {
        return shopeeInfoRepository.save(shopeeInfo);
    }

    // Retrieve ShopeeInfo by ID
    public Optional<ShopeeInfo> getShopeeInfoById(Long id) {
        return shopeeInfoRepository.findById(id);
    }

    // Retrieve all ShopeeInfo entries
    public List<ShopeeInfo> getAllShopeeInfo() {
        return shopeeInfoRepository.findAll();
    }

    // Delete ShopeeInfo by ID
    public void deleteShopeeInfoById(Long id) {
        shopeeInfoRepository.deleteById(id);
    }
}