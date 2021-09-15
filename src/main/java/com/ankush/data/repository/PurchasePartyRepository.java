package com.ankush.data.repository;

import com.ankush.data.entities.PurchaseParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchasePartyRepository extends JpaRepository<PurchaseParty,Integer> {

}
