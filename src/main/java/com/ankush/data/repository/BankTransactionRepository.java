package com.ankush.data.repository;

import com.ankush.data.entities.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction>getByBank(int bankid);
}