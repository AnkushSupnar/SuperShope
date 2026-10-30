package com.ankush.data.service;

import com.ankush.data.entities.Bill;
import com.ankush.data.entities.ItemStock;
import com.ankush.data.entities.Transaction;
import com.ankush.data.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {
    @Autowired
    private BillRepository repository;
    @Autowired
    private ItemStockService stockService;
    @Autowired
    private BillingCounterService counterService;

    /**
     * Final save of a new bill made on a counter: the bill, its stock reduction and the removal of the
     * counter's temporary lines happen together, so a bill can't be saved while its lines stay on the
     * counter (and get billed twice) or be lost.
     */
    @Transactional
    public void saveCounterBill(Bill bill, Integer counterno) {
        if (bill.getBillno() != null) throw new IllegalArgumentException("Not a new bill");
        repository.save(bill);
        for (Transaction tr : bill.getTransactions()) {
            stockService.saveItemStock(ItemStock.builder()
                    .stock(-tr.getQuantity())
                    .itemname(tr.getItemname())
                    .barcode(tr.getBarcode())
                    .unit(tr.getUnit())
                    .build());
        }
        if (counterno != null) counterService.clearCounter(counterno);
    }

    /**
     * Save of an edited bill in one transaction: the old lines' stock is given back, the bill and its new
     * lines are saved and their stock taken. If anything fails nothing changes (stock used to stay raised).
     */
    @Transactional
    public void saveEditedBill(Bill bill) {
        if (bill.getBillno() == null) throw new IllegalArgumentException("Not a saved bill");
        Bill old = repository.findById(bill.getBillno())
                .orElseThrow(() -> new IllegalArgumentException("Bill " + bill.getBillno() + " not found"));
        for (Transaction tr : old.getTransactions()) {
            stockService.saveItemStock(ItemStock.builder()
                    .stock(tr.getQuantity())
                    .itemname(tr.getItemname())
                    .barcode(tr.getBarcode())
                    .unit(tr.getUnit())
                    .build());
        }
        repository.save(bill);
        for (Transaction tr : bill.getTransactions()) {
            stockService.saveItemStock(ItemStock.builder()
                    .stock(-tr.getQuantity())
                    .itemname(tr.getItemname())
                    .barcode(tr.getBarcode())
                    .unit(tr.getUnit())
                    .build());
        }
    }

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

    public List<Bill> findByCustomerIdAndDateBetween(Integer customerId, LocalDate start, LocalDate end) {
        return repository.findByCustomerIdAndDateBetween(customerId, start, end);
    }

    public List<Bill> findByCustomerId(Integer customerId) {
        return repository.findByCustomerId(customerId);
    }
}
