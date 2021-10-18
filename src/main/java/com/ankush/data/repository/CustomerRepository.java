package com.ankush.data.repository;

import com.ankush.data.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query("select name from Customer")
    List<String>getAllCustomerNames();

    Customer getByName(String name);
    Customer getByContact(String contact);
}