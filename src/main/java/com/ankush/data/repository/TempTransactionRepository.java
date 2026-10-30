package com.ankush.data.repository;

import com.ankush.data.entities.TempTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempTransactionRepository extends JpaRepository<TempTransaction, Long> {
    List<TempTransaction> findByCounternoOrderByLinenoAsc(Integer counterno);

    long countByCounterno(Integer counterno);

    @Modifying
    @Query("delete from TempTransaction t where t.counterno = :counterno")
    void deleteByCounterno(@Param("counterno") Integer counterno);

    /** rows of: counterno, line count, total amount */
    @Query("select t.counterno, count(t), sum(t.amount) from TempTransaction t group by t.counterno")
    List<Object[]> getSummary();
}
