package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Customer;

import java.util.Date;

public interface CustomerService {
    Customer add(Customer customer);
    void update(Customer customer);
    void delete(int id);

    String findById(int id);
    String findByName(String name);
    String findAll();

    Iterable<Object[]> reportOrders(Date startDate, Date endDate, Boolean onlyCompleted);
}
