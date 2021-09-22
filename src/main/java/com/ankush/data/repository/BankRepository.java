package com.ankush.data.repository;

import com.ankush.data.entities.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {
    @Query("select bankname from Bank")
    List<String>getAllBankNames();

    Bank getBankByBankname(String bankname);

    @Query("Select balance from Bank where id=:id")
    Float getBankBalanceById(Integer id);

    @Transactional
    @Modifying
    @Query("update Bank set balance=balance-:amount where id=:bankid")
    void reduceBankBalance(Float amount,int bankid);

    @Transactional
    @Modifying
    @Query("update Bank set balance=balance+:amount where id=:bankid")
    void addBankBalance(Float amount,int bankid);
}