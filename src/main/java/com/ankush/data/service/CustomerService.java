package com.ankush.data.service;

import com.ankush.data.entities.Customer;
import com.ankush.data.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository repository;
    public List<Customer>getAllCustomer()
    {
        return repository.findAll();
    }
    public Customer getById(Integer id)
    {
        return repository.getById(id);
    }
    public Optional< Customer> findById(Integer id)
    {
        return repository.findById(id);
    }
    public int saveCustomer(Customer customer)
    {
        if(customer.getId()==null)
        {
            repository.save(customer);
            return 1;
        }
        else{
            repository.save(customer);
            return 2;
        }
    }
    public Customer getCustomerByName(String name)
    {
        return repository.getByName(name);
    }
    public Customer getCustomerByContact(String contact)
    {
        return repository.getByContact(contact);
    }
    public List<String>getAllCustomerNames()
    {
        return repository.getAllCustomerNames();
    }
}
