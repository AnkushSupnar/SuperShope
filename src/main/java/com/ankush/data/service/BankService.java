package com.ankush.data.service;

import com.ankush.data.entities.Bank;
import com.ankush.data.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BankService {
    @Autowired
    BankRepository repository;
    public List<Bank>getAllBank()
    {
        return repository.findAll();
    }
    public int saveBank(Bank bank){
        if(bank.getId()==null)
        {
            repository.save(bank);
            return 1;
        }
        else
        {
            repository.save(bank);
            return 2;
        }
    }
   public List<String> getAllBankNames(){
        return repository.getAllBankNames();
    }
    public Bank getBankByBankname(String bankname){
        return repository.getBankByBankname(bankname);
    }
    public Float getBalanceById(Integer id){
        return repository.getBankBalanceById(id);
    }
    public int addBankBalance(Float amount,int bankid)
    {
        Bank bank = repository.getById(bankid);
        bank.setBalance(bank.getBalance()+amount);
        repository.save(bank);
        return 1;
    }
    public int reduceBankBalance(Float amount,int bankid)
    {
        Bank bank = repository.getById(bankid);
        bank.setBalance(bank.getBalance()-amount);
        repository.save(bank);
        return 1;
    }
    public Bank getBankById(int id)
    {
        return repository.getById(id);
    }
    public Optional<Bank> getBankByIdOptional(Integer id) {
        return repository.findById(id);
    }

}
