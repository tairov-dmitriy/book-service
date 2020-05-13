package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Order;

public interface OrderService {
    Order add(Order order);
    void update(Order order);
    void delete(int id);

    String findById(int id);
    String findAll();

    String findByCustomerId(int id);
    void completeById(int id);
}
