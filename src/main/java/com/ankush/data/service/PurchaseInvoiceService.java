package com.ankush.data.service;

import com.ankush.data.entities.PurchaseInvoice;
import com.ankush.data.entities.PurchaseInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PurchaseInvoiceService {
    @Autowired
    PurchaseInvoiceRepository repository;

    public List<PurchaseInvoice>getAllPurchaseInvoice(){
        return repository.findAll();
    }
    public List<PurchaseInvoice>getDateWisePurchaseInvoice(LocalDate date){
        return repository.getByDate(date);
    }
    public List<PurchaseInvoice>getDatePeriodWisePurchaseInvoice(LocalDate start,LocalDate end){
        return repository.getByDatePeriod(start,end);
    }
    public int savePurchaseInvoice(PurchaseInvoice invoice){
        if(invoice.getId()==null)
        {
            repository.save(invoice);
            return 1;
        }
        else
        {
            repository.save(invoice);
            return 2;
        }
    }
    public PurchaseInvoice getInvoiceById(Long id)
    {
        return repository.getById(id);
    }
}
