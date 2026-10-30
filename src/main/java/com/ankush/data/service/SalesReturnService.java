package com.ankush.data.service;

import com.ankush.data.entities.SalesReturn;
import com.ankush.data.repository.SalesReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SalesReturnService {
    @Autowired
    private SalesReturnRepository repository;

    public int saveSalesReturn(SalesReturn salesReturn) {
        if (salesReturn.getId() == null) {
            repository.save(salesReturn);
            return 1;
        } else {
            repository.save(salesReturn);
            return 2;
        }
    }

    public List<SalesReturn> getAll() {
        return repository.findAll();
    }

    public List<SalesReturn> getByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    public List<SalesReturn> getByDateBetween(LocalDate start, LocalDate end) {
        return repository.findByDateBetween(start, end);
    }

    public List<SalesReturn> getByBillNo(Long billno) {
        return repository.findByBillNo(billno);
    }

    public Float getAlreadyReturnedQty(Long billno, String itemname, String barcode) {
        Float qty = repository.getReturnedQty(billno, itemname, barcode);
        return qty == null ? 0.0f : qty;
    }
}
