package com.ankush.data.repository;

import com.ankush.data.entities.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
    @Query("select p from PurchaseInvoice p where p.date = ?1")
    List<PurchaseInvoice> getByDate(LocalDate date);


    @Query("from PurchaseInvoice where date between :start and :end")
    List<PurchaseInvoice> getByDatePeriod(@Param("start")LocalDate start,@Param("end") LocalDate end);

    List<PurchaseInvoice> findByPurchaseParty_Id(Integer id);

    @Query("select p from PurchaseInvoice p where p.date between ?1 and ?2 ")
    List<PurchaseInvoice> getPurchaseInvoiceByDatePeriod(LocalDate dateStart, LocalDate dateEnd);
}