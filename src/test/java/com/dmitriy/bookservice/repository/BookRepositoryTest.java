package com.dmitriy.bookservice.repository;

import com.dmitriy.bookservice.model.Book;
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

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void getBook() {
        Book book = new Book("Book name", 2018, "Book annotation");
        entityManager.persist(book);
        entityManager.flush();

        List<Book> found = bookRepository.findByName(book.getName());
        assertThat(found.get(0).getName()).isEqualTo(book.getName());
        assertThat(found.get(0).getAnnotation()).isEqualTo(book.getAnnotation());
        assertThat(found.get(0).getPublicationYear()).isEqualTo(book.getPublicationYear());

        Optional<Book> found2 = bookRepository.findById(book.getId());
        assertTrue(found2.isPresent());
        assertThat(found2.get().getName()).isEqualTo(book.getName());

        List<Book> found3 = Lists.newArrayList(bookRepository.findAll());
        assertEquals(found3.size(), 1);
        assertThat(found3.get(0).getName()).isEqualTo(book.getName());
    }

    @Test
    public void addBook() {
        Book book = new Book("Book name", 2018, "Book annotation");

        Book saved = bookRepository.save(book);
        assertThat(saved).isEqualTo(book);

        List<Book> found = bookRepository.findByName("Book name");
        assertThat(found.get(0).getName()).isEqualTo("Book name");
        assertThat(found.get(0).getAnnotation()).isEqualTo("Book annotation");
        assertThat(found.get(0).getPublicationYear()).isEqualTo(2018);
    }

    @Test
    public void updateBook() {
        Book book = new Book("Book name", 2018, "Book annotation");

        bookRepository.save(book);

        Book updatedBook = new Book("Book name", 2019, "Book annotation. Edition 2");
        updatedBook.setId(book.getId());

        bookRepository.save(updatedBook);

        List<Book> found = bookRepository.findByName("Book name");
        assertThat(found.get(0).getName()).isEqualTo("Book name");
        assertThat(found.get(0).getAnnotation()).isEqualTo("Book annotation. Edition 2");
        assertThat(found.get(0).getPublicationYear()).isEqualTo(2019);
    }

    @Test
    public void deleteBook() {
        Book book = new Book("Book name", 2018, "Book annotation");

        bookRepository.save(book);

        bookRepository.deleteById(book.getId());

        List<Book> found = bookRepository.findByName("Book name");
        assertEquals(found.size(), 0);
        Optional<Book> found2 = bookRepository.findById(book.getId());
        assertFalse(found2.isPresent());
        List<Book> found3 = Lists.newArrayList(bookRepository.findAll());
        assertEquals(found3.size(), 0);
    }

    @Test
    public void validation() {
        Book book = new Book("", 2018, "Book annotation");

        try {
            bookRepository.save(book);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        book = new Book(null, 2018, "Book annotation");

        try {
            bookRepository.save(book);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        book = new Book("Book name", 2018, "");

        try {
            bookRepository.save(book);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        book = new Book("Book name", 2018, null);

        try {
            bookRepository.save(book);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        book = new Book("Book name", 0, "Book annotation");

        try {
            bookRepository.save(book);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        book = new Book("Book name", 2030, "Book annotation");

        try {
            bookRepository.save(book);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}
    }
}
