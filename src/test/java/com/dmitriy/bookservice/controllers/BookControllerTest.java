package com.dmitriy.bookservice.controllers;

import com.dmitriy.bookservice.model.Book;
import com.dmitriy.bookservice.service.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @TestConfiguration
    static class BookControllerTestContextConfiguration {

        @Bean
        @Primary
        ObjectMapper defaultMapper() {
            return new ObjectMapper();
        }

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
    }

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("mapperWithoutBooksRef")
    private ObjectMapper mapperWithoutBooksRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsRef")
    private ObjectMapper mapperWithoutAuthorsRef;

    private Book newBook = new Book("New book name", 2020, "Book annotation");

    @Before
    public void setUp() throws JsonProcessingException {
        Book book = new Book("Book name", 2018, "Book annotation");
        book.setId(1);
        List<Book> list = Arrays.asList(book);

        newBook.setId(2);

        Mockito.when(bookService.findAll()).thenReturn(mapperWithoutAuthorsRef.writeValueAsString(list));
        Mockito.when(bookService.findByName(book.getName())).thenReturn(mapperWithoutBooksRef.writeValueAsString(list));
        Mockito.when(bookService.findById(book.getId())).thenReturn(mapperWithoutBooksRef.writeValueAsString(book));
        Mockito.when(bookService.add(newBook)).thenReturn(newBook);
    }

    @Test
    public void getBooks() throws Exception {
        mvc.perform(get("/api/getBooks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Book name")))
                .andExpect(jsonPath("$[0].publicationYear", is(2018)))
                .andExpect(jsonPath("$[0].annotation", is("Book annotation")));

        mvc.perform(get("/api/getBooksByName?name=Book name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Book name")))
                .andExpect(jsonPath("$[0].publicationYear", is(2018)))
                .andExpect(jsonPath("$[0].annotation", is("Book annotation")));

        mvc.perform(get("/api/getBookById?id=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Book name")))
                .andExpect(jsonPath("$.publicationYear", is(2018)))
                .andExpect(jsonPath("$.annotation", is("Book annotation")));
    }

    @Test
    public void addBook() throws Exception {
        mvc.perform(post("/api/addBook")
                .content(mapperWithoutBooksRef.writeValueAsString(newBook))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));
    }
}
