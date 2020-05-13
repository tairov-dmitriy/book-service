package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Order;
import com.dmitriy.bookservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    @Qualifier("mapperWithoutBooksAndOrdersRef")
    private ObjectMapper mapperWithoutBooksAndOrdersRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsAndOrdersRef")
    private ObjectMapper mapperWithoutAuthorsAndOrdersRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsAndCustomerRef")
    private ObjectMapper mapperWithoutAuthorsAndCustomerRef;

    @Transactional
    @Override
    public Order add(Order order) {
        if (order.getId() != 0)
            throw new IllegalArgumentException("ID of new order generate automatically and must be equal 0 or absent");

        return orderRepository.save(order);
    }

    @Transactional
    @Override
    public void update(Order order) {
        if (!orderRepository.existsById(order.getId()))
            throw new IllegalArgumentException("Order (id = " + order.getId() + ") not found");

        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void delete(int id) {
        orderRepository.deleteById(id);
    }

    @Transactional
    @Override
    public String findById(int id) {
        Optional<Order> order = orderRepository.findById(id);

        if (!order.isPresent())
            throw new IllegalStateException("Order (id = " + id + ") not found");

        try {
            return mapperWithoutAuthorsAndOrdersRef.writeValueAsString(order.get());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Order to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    @Override
    public String findByCustomerId(int id) {
        Iterable<Order> list = orderRepository.findByCustomerId(id);

        try {
            return mapperWithoutAuthorsAndCustomerRef.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Order to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    @Override
    public String findAll() {
        Iterable<Order> list = orderRepository.findAll();
        try {
            return mapperWithoutBooksAndOrdersRef.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Order to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    @Override
    public void completeById(int id) {
        Optional<Order> opt = orderRepository.findById(id);
        if (!opt.isPresent())
            throw new IllegalArgumentException("Order (id = " + id + ") not found");

        Order order = opt.get();
        if (order.getCompleted())
            throw new IllegalArgumentException("Order (id = " + id + ") already completed");

        order.setCompleteDate(new Date());
        order.setCompleted(true);

        orderRepository.save(order);
    }
}
