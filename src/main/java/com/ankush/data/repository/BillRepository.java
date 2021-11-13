package com.ankush.data.repository;

import com.ankush.data.entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill>getByDate(LocalDate date);

    List<Bill>findByDateBetween(LocalDate start,LocalDate end);
    @Query("select max(billno) from Bill")
    Long getlastBillNo();

    @Query("select sum(grandtotal) from Bill where date=:date")
    Float getDateTotalSale(LocalDate date);

    @Query("select count(billno) from Bill where date=:date")
    int getDateTotalBill(LocalDate date);





}