package com.ankush.data.repository;

import com.ankush.data.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // [itemname, number of bill lines] for bills since the given date
    @Query("select t.itemname, count(t) from Transaction t where t.bill.date >= :since group by t.itemname")
    List<Object[]> getSaleCountSince(@Param("since") LocalDate since);

    // [itemname, unit, sale amount, quantity] per item for a period
    @Query("select t.itemname, t.unit, sum(t.amount), sum(t.quantity) from Transaction t "
            + "where t.bill.date between :start and :end group by t.itemname, t.unit")
    List<Object[]> getItemSales(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // sum of all bill line amounts for a period
    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.bill.date between :start and :end")
    Double getLineTotal(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // [amount, purchase cost] of the bill lines that have a purchase rate (HQL 5 has no arithmetic inside CASE)
    @Query("select coalesce(sum(t.amount), 0), coalesce(sum(t.price * t.quantity), 0) from Transaction t "
            + "where t.bill.date between :start and :end and t.price > 0")
    List<Object[]> getCostedLineTotals(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
