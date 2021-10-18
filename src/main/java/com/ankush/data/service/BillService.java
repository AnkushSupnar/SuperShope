package com.ankush.data.service;

import com.ankush.data.entities.Bill;
import com.ankush.data.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {
    @Autowired
    private BillRepository repository;

    public List<Bill>getAllBills(){return repository.findAll();}
    public Bill getBillByBillNo(long billNo){return repository.getById(billNo);}
    public List<Bill>getBillByDate(LocalDate date){return repository.getByDate(date);}
    public List<Bill>findByDateBetween(LocalDate start,LocalDate end){return repository.findByDateBetween(start,end);}

    public int saveBill(Bill bill)
    {
        if(bill.getBillno()==null)
        {
            repository.save(bill);
            return 1;
        }
        else
        {
            repository.save(bill);
            return 2;
        }
    }

}
