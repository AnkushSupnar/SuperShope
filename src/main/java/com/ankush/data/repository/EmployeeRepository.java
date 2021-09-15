package com.ankush.data.repository;

import com.ankush.data.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    @Query("select name from Employee")
    List<String>getAllEmployeeNames();
    Employee getById(Integer id);
    Employee getByName(String name);

}
