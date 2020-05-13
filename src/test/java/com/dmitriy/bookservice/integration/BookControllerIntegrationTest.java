package com.dmitriy.bookservice.integration;

import com.dmitriy.bookservice.BookserviceApplication;
import com.dmitriy.bookservice.model.Book;
import com.dmitriy.bookservice.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.jayway.jsonpath.JsonPath;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = BookserviceApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(locations = "classpath:test.properties")
@Sql(scripts = "classpath:test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookRepository bookRepository;

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
    }

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("mapperWithoutBooksRef")
    private ObjectMapper mapperWithoutBooksRef;

    @After
    public void resetDb() {
        bookRepository.deleteAll();
    }

    @Test
    public void getBooks() throws Exception {

        resetDb();

        Book book = new Book("Book name", 2018, "Book annotation");
        bookRepository.save(book);

        mvc.perform(get("/api/getBooks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(book.getName())))
                .andExpect(jsonPath("$[0].publicationYear", is(book.getPublicationYear())))
                .andExpect(jsonPath("$[0].annotation", is(book.getAnnotation())));

        MvcResult result = mvc.perform(get("/api/getBooksByName?name=Book name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(book.getName())))
                .andExpect(jsonPath("$[0].publicationYear", is(book.getPublicationYear())))
                .andExpect(jsonPath("$[0].annotation", is(book.getAnnotation())))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$[0].id");

        mvc.perform(get("/api/getBookById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(book.getName())))
                .andExpect(jsonPath("$.publicationYear", is(book.getPublicationYear())))
                .andExpect(jsonPath("$.annotation", is(book.getAnnotation())));
    }

    @Test
    public void addBook() throws Exception {

        resetDb();

        Book book = new Book("Book name", 2018, "Book annotation");
        bookRepository.save(book);

        Book newBook = new Book("New book name", 2020, "Book annotation");

        MvcResult result = mvc.perform(post("/api/addBook")
                .content(mapperWithoutBooksRef.writeValueAsString(newBook))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$.id");

        mvc.perform(get("/api/getBooksByName?name=New book name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(newBook.getName())))
                .andExpect(jsonPath("$[0].publicationYear", is(newBook.getPublicationYear())))
                .andExpect(jsonPath("$[0].annotation", is(newBook.getAnnotation())));

        mvc.perform(get("/api/getBookById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(newBook.getName())))
                .andExpect(jsonPath("$.publicationYear", is(newBook.getPublicationYear())))
                .andExpect(jsonPath("$.annotation", is(newBook.getAnnotation())));
    }

    @Test
    public void updateBook() throws Exception {

        resetDb();

        Book book = new Book("Book name", 2018, "Book annotation");
        book = bookRepository.save(book);

        Book updatedBook = new Book("Book name", 2020, "Updated book annotation");
        updatedBook.setId(book.getId());

        mvc.perform(post("/api/updateBook")
                .content(mapperWithoutBooksRef.writeValueAsString(updatedBook))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/getBooksByName?name=Book name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(updatedBook.getName())))
                .andExpect(jsonPath("$[0].publicationYear", is(updatedBook.getPublicationYear())))
                .andExpect(jsonPath("$[0].annotation", is(updatedBook.getAnnotation())));

        mvc.perform(get("/api/getBookById?id=" + book.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updatedBook.getName())))
                .andExpect(jsonPath("$.publicationYear", is(updatedBook.getPublicationYear())))
                .andExpect(jsonPath("$.annotation", is(updatedBook.getAnnotation())));
    }

    @Test
    public void deleteBook() throws Exception {
        resetDb();

        Book book = new Book("Book name", 2018, "Book annotation");
        book = bookRepository.save(book);

        mvc.perform(get("/api/deleteBook?id=" + book.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/getBooksByName?name=Book name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        try {
            mvc.perform(get("/api/getBookById?id=" + book.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            fail("Not existed book was founded");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("Book (id = " + book.getId() + ") not found"));
        }
    }
}
