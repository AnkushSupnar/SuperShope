package com.ankush.data.repository;

import com.ankush.data.entities.PurchaseParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasePartyRepository extends JpaRepository<PurchaseParty,Integer> {
    @Query("select name from PurchaseParty")
    List<String> getAllPurchasePartyNames();
    PurchaseParty getByName(String name);
}
