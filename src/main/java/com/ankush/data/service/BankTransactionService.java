package com.ankush.data.service;

import com.ankush.data.entities.BankTransaction;
import com.ankush.data.repository.BankTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankTransactionService {
    @Autowired
    private BankTransactionRepository repository;

    public BankTransaction getBankTransactionById(Long id)
    {
        return repository.getById(id);
    }
    public List<BankTransaction>getAllBankTransaction()
    {
        return repository.findAll();
    }
    public List<BankTransaction>getBankTransactionByBankId(int bankid)
    {
        return repository.getByBank(bankid);
    }
    public int saveBankTransaction(BankTransaction bankTransaction)
    {
        if(bankTransaction.getId()==null)
        {
            repository.save(bankTransaction);
            return 1;
        }
        else
        {
            repository.save(bankTransaction);
            return 2;
        }
    }
}
