package com.ankush.data.repository;

import com.ankush.data.entities.ShopeeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopeeInfoRepository extends JpaRepository<ShopeeInfo, Long> {

}
