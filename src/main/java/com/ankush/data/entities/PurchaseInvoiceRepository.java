package com.ankush.data.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
    @Query("select p from PurchaseInvoice p where p.date = ?1")
    List<PurchaseInvoice> getByDate(LocalDate date);


    @Query("from PurchaseInvoice where date between :start and :end")
    List<PurchaseInvoice> getByDatePeriod(LocalDate start, LocalDate end);

    List<PurchaseInvoice> findByPurchaseParty_Id(Integer id);



    @Query("select p from PurchaseInvoice p where p.date between ?1 and ?2 ")
    List<PurchaseInvoice> getPurchaseInvoiceByDatePeriod(LocalDate dateStart, LocalDate dateEnd);


}