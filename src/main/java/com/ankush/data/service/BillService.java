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

    public Long getLastBill()
    {
        return repository.getlastBillNo();
    }

    public Float getDateTotalSale(LocalDate date)
    {
        if(repository.getDateTotalSale(date)==null) return 0.0f;
        else
        return repository.getDateTotalSale(date);
    }
    public int getDateTotalBill(LocalDate date){

        return repository.getDateTotalBill(date);
    }
    public int getPeriodBillCount(LocalDate start,LocalDate end)
    {
        return repository.getPeriodBillCount(start,end);
    }
    public Float getPeriodBillAmount(LocalDate start,LocalDate end)
    {
        if(repository.getPariodBillAmount(start,end)==null) return 0.0f;
        else return repository.getPariodBillAmount(start,end);
    }
    public Float getMonthlyBillAmount(LocalDate start,LocalDate end)
    {
        System.out.println(start+" "+end);
        if(repository.getMonthlyBillAmount(start,end)==null) return 0.0f;
        else return repository.getMonthlyBillAmount(start,end);
    }

}
