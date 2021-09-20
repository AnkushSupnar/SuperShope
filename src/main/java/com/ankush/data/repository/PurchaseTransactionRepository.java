package com.ankush.data.repository;

import com.ankush.data.entities.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {

    @Transactional
    @Modifying
    @Query("delete from PurchaseTransaction where id=:id")
    void deleteTransaction(Long id);
}