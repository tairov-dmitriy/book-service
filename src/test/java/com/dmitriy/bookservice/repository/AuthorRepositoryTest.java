package com.dmitriy.bookservice.repository;

import com.dmitriy.bookservice.model.Author;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
public class AuthorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    public void getAuthor() {
        Author author = new Author("Author name", 1980);
        entityManager.persist(author);
        entityManager.flush();

        List<Author> found = authorRepository.findByFullName(author.getFullName());
        assertThat(found.get(0).getFullName()).isEqualTo(author.getFullName());
        assertThat(found.get(0).getBirthYear()).isEqualTo(author.getBirthYear());

        Optional<Author> found2 = authorRepository.findById(author.getId());
        assertTrue(found2.isPresent());
        assertThat(found2.get().getFullName()).isEqualTo(author.getFullName());

        List<Author> found3 = Lists.newArrayList(authorRepository.findAll());
        assertEquals(found3.size(), 1);
        assertThat(found3.get(0).getFullName()).isEqualTo(author.getFullName());
    }

    @Test
    public void addAuthor() {
        Author author = new Author("Author name", 1980);

        Author saved = authorRepository.save(author);
        assertThat(saved).isEqualTo(author);

        List<Author> found = authorRepository.findByFullName("Author name");
        assertThat(found.get(0).getFullName()).isEqualTo("Author name");
        assertThat(found.get(0).getBirthYear()).isEqualTo(1980);
    }

    @Test
    public void updateAuthor() {
        Author author = new Author("Author name", 1980);

        authorRepository.save(author);

        Author updatedAuthor = new Author("Author name 2", 1983);
        updatedAuthor.setId(author.getId());

        authorRepository.save(updatedAuthor);

        List<Author> found = authorRepository.findByFullName("Author name");
        assertEquals(found.size(), 0);
        List<Author> found2 = authorRepository.findByFullName("Author name 2");
        assertEquals(found2.size(), 1);
        assertThat(found2.get(0).getFullName()).isEqualTo("Author name 2");
        assertThat(found2.get(0).getBirthYear()).isEqualTo(1983);
    }

    @Test
    public void deleteAuthor() {
        Author author = new Author("Author name", 1980);

        authorRepository.save(author);

        authorRepository.deleteById(author.getId());

        List<Author> found = authorRepository.findByFullName("Author name");
        assertEquals(found.size(), 0);
        Optional<Author> found2 = authorRepository.findById(author.getId());
        assertFalse(found2.isPresent());
        List<Author> found3 = Lists.newArrayList(authorRepository.findAll());
        assertEquals(found3.size(), 0);
    }

    @Test
    public void validation() {
        Author author = new Author("", 1980);

        try {
            authorRepository.save(author);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        author = new Author(null, 1980);

        try {
            authorRepository.save(author);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        author = new Author("Author name", 0);

        try {
            authorRepository.save(author);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        author = new Author("Author name", 2025);

        try {
            authorRepository.save(author);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}
    }

}
