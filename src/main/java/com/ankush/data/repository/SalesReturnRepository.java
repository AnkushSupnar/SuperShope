package com.ankush.data.repository;

import com.ankush.data.entities.SalesReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {

    @Query("select s from SalesReturn s where s.date = :date order by s.id desc")
    List<SalesReturn> findByDate(@Param("date") LocalDate date);

    @Query("select s from SalesReturn s where s.date between :start and :end order by s.id desc")
    List<SalesReturn> findByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("select s from SalesReturn s where s.bill.billno = :billno order by s.id desc")
    List<SalesReturn> findByBillNo(@Param("billno") Long billno);

    @Query("select coalesce(sum(srt.quantity),0) from SalesReturnTransaction srt where srt.salesReturn.bill.billno = :billno and srt.itemname = :itemname and srt.barcode = :barcode")
    Float getReturnedQty(@Param("billno") Long billno, @Param("itemname") String itemname, @Param("barcode") String barcode);
}
