package com.dmitriy.bookservice.repository;

import com.dmitriy.bookservice.model.Author;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer> {
    List<Author> findByFullName(String fullName);
}
