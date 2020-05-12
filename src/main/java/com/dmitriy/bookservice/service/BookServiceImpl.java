package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Book;
import com.dmitriy.bookservice.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    @Qualifier("mapperWithoutBooksRef")
    private ObjectMapper mapperWithoutBooksRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsRef")
    private ObjectMapper mapperWithoutAuthorsRef;

    @Transactional
    public String findById(int id) {
        Optional<Book> book = bookRepository.findById(id);

        if (!book.isPresent())
            throw new IllegalStateException("Book (id = " + id + ") not found");

        try {
            return mapperWithoutBooksRef.writeValueAsString(book.get());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Book to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    public String findByName(String name) {
        List<Book> list = bookRepository.findByName(name);
        try {
            return mapperWithoutBooksRef.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Book to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    public String findAll() {
        Iterable<Book> list = bookRepository.findAll();
        try {
            return mapperWithoutAuthorsRef.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Book to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    public Book add(Book book) {
        if (book.getId() != 0)
            throw new IllegalArgumentException("ID of new book generate automatically and must be equal 0 or absent");

        return bookRepository.save(book);
    }

    @Transactional
    public void update(Book book) {
        if (!bookRepository.existsById(book.getId()))
            throw new IllegalArgumentException("Book (id = " + book.getId() + ") not found");

        bookRepository.save(book);
    }

    @Transactional
    public void delete(int id) {
        bookRepository.deleteById(id);
    }
}
