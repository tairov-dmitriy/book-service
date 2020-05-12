package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Book;
import com.dmitriy.bookservice.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class BookServiceTest {

    @TestConfiguration
    static class BookServiceImplTestContextConfiguration {

        @Bean("mapperWithoutBooksRef")
        ObjectMapper mapperWithoutBooksRef() {
            ObjectMapper mapperWithoutBooksRef = new ObjectMapper();
            mapperWithoutBooksRef.setFilterProvider(new SimpleFilterProvider().addFilter("nestedFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("books")));
            return mapperWithoutBooksRef;
        }

        @Bean("mapperWithoutAuthorsRef")
        ObjectMapper mapperWithoutAuthorsRef() {
            ObjectMapper mapperWithoutAuthorsRef = new ObjectMapper();
            mapperWithoutAuthorsRef.setFilterProvider(new SimpleFilterProvider().addFilter("nestedFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("authors")));
            return mapperWithoutAuthorsRef;
        }

        @Bean
        public BookService bookService() {
            return new BookServiceImpl();
        }
    }

    @Autowired
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @Before
    public void setUp() {
        Book book = new Book("Book name", 2018, "Book annotation");
        book.setId(1);
        List<Book> list = Arrays.asList(book);

        Mockito.when(bookRepository.findByName(book.getName())).thenReturn(list);
        Mockito.when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        Mockito.when(bookRepository.findAll()).thenReturn(list);
    }

    @Test
    public void getJSONBooks() {
        String answer = bookService.findByName("Book name");
        assertThat(answer).isEqualTo("[{\"id\":1,\"name\":\"Book name\",\"publicationYear\":2018,\"annotation\":\"Book annotation\",\"authors\":[]}]");

        answer = bookService.findById(1);
        assertThat(answer).isEqualTo("{\"id\":1,\"name\":\"Book name\",\"publicationYear\":2018,\"annotation\":\"Book annotation\",\"authors\":[]}");

        answer = bookService.findAll();
        assertThat(answer).isEqualTo("[{\"id\":1,\"name\":\"Book name\",\"publicationYear\":2018,\"annotation\":\"Book annotation\"}]");
    }
}
