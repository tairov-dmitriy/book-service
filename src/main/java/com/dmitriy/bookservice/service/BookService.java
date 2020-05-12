package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Book;

public interface BookService {
    Book add(Book book);
    void update(Book book);
    void delete(int id);

    String findById(int id);
    String findByName(String name);
    String findAll();
}
