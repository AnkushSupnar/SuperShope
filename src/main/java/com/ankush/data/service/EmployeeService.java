package com.ankush.data.service;

import com.ankush.data.entities.Employee;
import com.ankush.data.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    EmployeeRepository repository;
    public int saveEmployee(Employee emp)
    {
        repository.save(emp);
        return 1;
    }
    public List<Employee> getAllEmployee() {
        return repository.findAll();
    }
    public Employee getEmployeeById(Integer id)
    {
        return repository.getById(id);
    }
   public List<String>getAllEmployeeNames()
    {
        return repository.getAllEmployeeNames();
    }
    public Employee getByName(String name)
    {
        return repository.getByName(name);
    }
}
