package com.ankush.data.repository;

import com.ankush.data.entities.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {
    @Query("select bankname from Bank")
    List<String>getAllBankNames();
    Bank getBankByBankname(String bankname);
    float getBalanceById(Integer id);
}