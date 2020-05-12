package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Author;
import com.dmitriy.bookservice.repository.AuthorRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    @Qualifier("mapperWithoutBooksRef")
    private ObjectMapper mapperWithoutBooksRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsRef")
    private ObjectMapper mapperWithoutAuthorsRef;

    @Transactional
    public String findById(int id) {
        Optional<Author> author = authorRepository.findById(id);

        if (!author.isPresent())
            throw new IllegalStateException("Author (id = " + id + ") not found");

        try {
            return mapperWithoutAuthorsRef.writeValueAsString(author.get());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Author to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    public String findByFullName(String fullName) {
        List<Author> list = authorRepository.findByFullName(fullName);
        try {
            return mapperWithoutAuthorsRef.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Author to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    public String findAll() {
        Iterable<Author> list = authorRepository.findAll();
        try {
            return mapperWithoutBooksRef.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed convert Author to JSON: " + ex.getMessage());
        }
    }

    @Transactional
    public Author add(Author author) {
        if (author.getId() != 0)
            throw new IllegalArgumentException("ID of new author generate automatically and must be equal 0 or absent");

        return authorRepository.save(author);
    }

    @Transactional
    public void update(Author author) {
        if (!authorRepository.existsById(author.getId()))
            throw new IllegalArgumentException("Author (id = " + author.getId() + ") not found");

        authorRepository.save(author);
    }

    @Transactional
    public void delete(int id) {
        authorRepository.deleteById(id);
    }
}
