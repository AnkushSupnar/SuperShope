package com.ankush.data.repository;

import com.ankush.data.entities.BillingCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingCounterRepository extends JpaRepository<BillingCounter, Integer> {
    List<BillingCounter> findAllByOrderByCounternoAsc();

    BillingCounter getByCounterno(Integer counterno);
}
