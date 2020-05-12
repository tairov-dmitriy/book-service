package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Author;

public interface AuthorService {
    Author add(Author author);
    void update(Author author);
    void delete(int id);

    String findById(int id);
    String findByFullName(String fullName);
    String findAll();
}
