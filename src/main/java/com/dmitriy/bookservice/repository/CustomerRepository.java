package com.dmitriy.bookservice.repository;

import com.dmitriy.bookservice.model.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Integer> {
    List<Customer> findByName(String name);

    @Query(value = "SELECT customers.name, customers.phone, COUNT(books.id) " +
            "FROM customers, orders, orders_books, books " +
            "WHERE customers.id = orders.customer_id AND orders.id = orders_books.order_id AND orders_books.book_id = books.id " +
            "AND orders.creationDate >= ?1 AND orders.creationDate <= ?2 " +
            "GROUP BY customers.id", nativeQuery = true)
    List<Object[]> reportOrders(Date startDate, Date endDate);

    @Query(value = "SELECT customers.name, customers.phone, COUNT(books.id) " +
            "FROM customers, orders, orders_books, books " +
            "WHERE customers.id = orders.customer_id AND orders.id = orders_books.order_id AND orders_books.book_id = books.id " +
            "AND orders.creationDate >= ?1 AND orders.creationDate <= ?2 AND orders.completed = true " +
            "GROUP BY customers.id", nativeQuery = true)
    List<Object[]> reportOrdersOnlyCompleted(Date startDate, Date endDate);

    @Query(value = "SELECT customers.name, customers.phone, COUNT(books.id), BOOL_AND(orders.completed) " +
            "FROM customers, orders, orders_books, books " +
            "WHERE customers.id = orders.customer_id AND orders.id = orders_books.order_id AND orders_books.book_id = books.id " +
            "AND orders.creationDate >= ?1 AND orders.creationDate <= ?2 " +
            "GROUP BY customers.id", nativeQuery = true)
    List<Object[]> reportOrdersWithCompletedFlag(Date startDate, Date endDate);
}
