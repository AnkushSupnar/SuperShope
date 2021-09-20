package com.ankush.data.service;

import com.ankush.data.entities.PurchaseTransaction;
import com.ankush.data.repository.PurchaseTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseTransactionService {
    @Autowired
    private PurchaseTransactionRepository repository;
    public int saveTransactions(List<PurchaseTransaction> trList)
    {
        repository.saveAll(trList);
        return 1;
    }
    public void deleteTransactions(List<PurchaseTransaction>trList)
    {
       for(PurchaseTransaction tr:trList)
       {
           System.out.println("Deleting Transaction");
           repository.deleteTransaction(tr.getId());
       }
    }
}
