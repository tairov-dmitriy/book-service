package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.repository.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    @Qualifier("mapperWithoutOrdersRef")
    private ObjectMapper mapperWithoutOrdersRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsAndCustomerRef")
    private ObjectMapper mapperWithoutAuthorsAndCustomerRef;

    @Transactional
    @Override
    public Customer add(Customer customer) {
        if (customer.getId() != 0)
            throw new IllegalArgumentException("ID of new customer generate automatically and must be equal 0 or absent");

        return customerRepository.save(customer);
    }

    @Transactional
    @Override
    public void update(Customer customer) {
        if (!customerRepository.existsById(customer.getId()))
            throw new IllegalArgumentException("Customer (id = " + customer.getId() + ") not found");

        customerRepository.save(customer);
    }

    @Transactional
    @Override
    public void delete(int id) {
        customerRepository.deleteById(id);
    }

    @Transactional
    @Override
    public String findById(int id) {
        Optional<Customer> customer = customerRepository.findById(id);

        if (!customer.isPresent())
            throw new IllegalStateException("Customer (id = " + id + ") not found");

        try {
            return mapperWithoutAuthorsAndCustomerRef.writeValueAsString(customer.get());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Customer to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    @Override
    public String findByName(String name) {
        List<Customer> list = customerRepository.findByName(name);
        try {
            return mapperWithoutAuthorsAndCustomerRef.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Customer to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    @Override
    public String findAll() {
        Iterable<Customer> list = customerRepository.findAll();
        try {
            return mapperWithoutOrdersRef.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Customer to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    @Override
    public Iterable<Object[]> reportOrders(Date startDate, Date endDate, Boolean onlyCompleted) {
        return onlyCompleted == null ?
                customerRepository.reportOrders(startDate, endDate) :
               onlyCompleted ?
                customerRepository.reportOrdersOnlyCompleted(startDate, endDate) :
                customerRepository.reportOrdersWithCompletedFlag(startDate, endDate);
    }
}
