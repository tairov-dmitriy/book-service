package com.dmitriy.bookservice.integration;

import com.dmitriy.bookservice.BookserviceApplication;
import com.dmitriy.bookservice.model.Author;
import com.dmitriy.bookservice.model.Book;
import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.model.Order;
import com.dmitriy.bookservice.repository.AuthorRepository;
import com.dmitriy.bookservice.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.core.IsNull;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = BookserviceApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(locations = "classpath:test.properties")
@Sql(scripts = "classpath:test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MainIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @TestConfiguration
    static class MainTestContextConfiguration {

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

        @Bean("mapperWithoutOrdersRef")
        ObjectMapper mapperWithoutOrdersRef() {
            ObjectMapper mapperWithoutAuthorsRef = new ObjectMapper();
            mapperWithoutAuthorsRef.setFilterProvider(new SimpleFilterProvider().addFilter("nestedFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("orders")));
            return mapperWithoutAuthorsRef;
        }

        @Bean("mapperWithoutAuthorsAndOrdersRef")
        ObjectMapper mapperWithoutAuthorsAndOrdersRef() {
            ObjectMapper mapperWithoutAuthorsAndOrdersRef = new ObjectMapper();
            mapperWithoutAuthorsAndOrdersRef.setFilterProvider(new SimpleFilterProvider().addFilter("nestedFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("authors", "orders")));
            return mapperWithoutAuthorsAndOrdersRef;
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

    @Autowired
    @Qualifier("mapperWithoutOrdersRef")
    private ObjectMapper mapperWithoutOrdersRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsAndOrdersRef")
    private ObjectMapper mapperWithoutAuthorsAndOrdersRef;

    @After
    public void resetDb() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    public void addBookWithAuthors() throws Exception {

        resetDb();

        Author newAuthor1 = new Author("Author name 1", 1986);

        MvcResult result = mvc.perform(post("/api/addAuthor")
                .content(mapperWithoutAuthorsRef.writeValueAsString(newAuthor1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        newAuthor1.setId(JsonPath.parse(response).read("$.id"));

        Author newAuthor2 = new Author("Author name 2", 1987);

        result = mvc.perform(post("/api/addAuthor")
                .content(mapperWithoutAuthorsRef.writeValueAsString(newAuthor2))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        response = result.getResponse().getContentAsString();
        newAuthor2.setId(JsonPath.parse(response).read("$.id"));

        Book newBook = new Book("New book name", 2019, "Book annotation");
        newBook.getAuthors().add(newAuthor1);
        newBook.getAuthors().add(newAuthor2);

        result = mvc.perform(post("/api/addBook")
                .content(mapperWithoutBooksRef.writeValueAsString(newBook))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$.id");

        mvc.perform(get("/api/getBookById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(newBook.getName())))
                .andExpect(jsonPath("$.publicationYear", is(newBook.getPublicationYear())))
                .andExpect(jsonPath("$.annotation", is(newBook.getAnnotation())))
                .andExpect(jsonPath("$.authors[0].id", is(newAuthor1.getId())))
                .andExpect(jsonPath("$.authors[0].fullName", is(newAuthor1.getFullName())))
                .andExpect(jsonPath("$.authors[0].birthYear", is(newAuthor1.getBirthYear())))
                .andExpect(jsonPath("$.authors[1].id", is(newAuthor2.getId())))
                .andExpect(jsonPath("$.authors[1].fullName", is(newAuthor2.getFullName())))
                .andExpect(jsonPath("$.authors[1].birthYear", is(newAuthor2.getBirthYear())));

        mvc.perform(get("/api/getAuthorById?id=" + newAuthor1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is(newAuthor1.getFullName())))
                .andExpect(jsonPath("$.birthYear", is(newAuthor1.getBirthYear())))
                .andExpect(jsonPath("$.books[0].id", is(id)))
                .andExpect(jsonPath("$.books[0].name", is(newBook.getName())))
                .andExpect(jsonPath("$.books[0].publicationYear", is(newBook.getPublicationYear())))
                .andExpect(jsonPath("$.books[0].annotation", is(newBook.getAnnotation())));

        mvc.perform(get("/api/getAuthorById?id=" + newAuthor2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is(newAuthor2.getFullName())))
                .andExpect(jsonPath("$.birthYear", is(newAuthor2.getBirthYear())))
                .andExpect(jsonPath("$.books[0].id", is(id)))
                .andExpect(jsonPath("$.books[0].name", is(newBook.getName())))
                .andExpect(jsonPath("$.books[0].publicationYear", is(newBook.getPublicationYear())))
                .andExpect(jsonPath("$.books[0].annotation", is(newBook.getAnnotation())));
    }

    @Test
    public void addAuthorWithBooks() throws Exception {

        resetDb();

        Book newBook1 = new Book("New book name", 2011, "Book annotation");

        MvcResult result = mvc.perform(post("/api/addBook")
                .content(mapperWithoutBooksRef.writeValueAsString(newBook1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        newBook1.setId(JsonPath.parse(response).read("$.id"));

        Book newBook2 = new Book("New book name 2", 2014, "Book annotation 2");

        result = mvc.perform(post("/api/addBook")
                .content(mapperWithoutBooksRef.writeValueAsString(newBook2))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        response = result.getResponse().getContentAsString();
        newBook2.setId(JsonPath.parse(response).read("$.id"));

        Author newAuthor = new Author("New author name", 1983);
        newAuthor.getBooks().add(newBook1);
        newAuthor.getBooks().add(newBook2);

        result = mvc.perform(post("/api/addAuthor")
                .content(mapperWithoutAuthorsRef.writeValueAsString(newAuthor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$.id");

        mvc.perform(get("/api/getAuthorById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is(newAuthor.getFullName())))
                .andExpect(jsonPath("$.birthYear", is(newAuthor.getBirthYear())))
                .andExpect(jsonPath("$.books[0].id", is(newBook1.getId())))
                .andExpect(jsonPath("$.books[0].name", is(newBook1.getName())))
                .andExpect(jsonPath("$.books[0].publicationYear", is(newBook1.getPublicationYear())))
                .andExpect(jsonPath("$.books[0].annotation", is(newBook1.getAnnotation())))
                .andExpect(jsonPath("$.books[1].id", is(newBook2.getId())))
                .andExpect(jsonPath("$.books[1].name", is(newBook2.getName())))
                .andExpect(jsonPath("$.books[1].publicationYear", is(newBook2.getPublicationYear())))
                .andExpect(jsonPath("$.books[1].annotation", is(newBook2.getAnnotation())));

        mvc.perform(get("/api/getBookById?id=" + newBook1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(newBook1.getName())))
                .andExpect(jsonPath("$.publicationYear", is(newBook1.getPublicationYear())))
                .andExpect(jsonPath("$.annotation", is(newBook1.getAnnotation())))
                .andExpect(jsonPath("$.authors[0].id", is(id)))
                .andExpect(jsonPath("$.authors[0].fullName", is(newAuthor.getFullName())))
                .andExpect(jsonPath("$.authors[0].birthYear", is(newAuthor.getBirthYear())));

        mvc.perform(get("/api/getBookById?id=" + newBook2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(newBook2.getName())))
                .andExpect(jsonPath("$.publicationYear", is(newBook2.getPublicationYear())))
                .andExpect(jsonPath("$.annotation", is(newBook2.getAnnotation())))
                .andExpect(jsonPath("$.authors[0].id", is(id)))
                .andExpect(jsonPath("$.authors[0].fullName", is(newAuthor.getFullName())))
                .andExpect(jsonPath("$.authors[0].birthYear", is(newAuthor.getBirthYear())));
    }

    @Test
    public void addAuthorBooksCustomerOrders() throws Exception {

        resetDb();

        Book newBook1 = new Book("New book name", 2011, "Book annotation");

        MvcResult result = mvc.perform(post("/api/addBook")
                .content(mapperWithoutBooksRef.writeValueAsString(newBook1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        newBook1.setId(JsonPath.parse(response).read("$.id"));

        Book newBook2 = new Book("New book name 2", 2014, "Book annotation 2");

        result = mvc.perform(post("/api/addBook")
                .content(mapperWithoutBooksRef.writeValueAsString(newBook2))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        response = result.getResponse().getContentAsString();
        newBook2.setId(JsonPath.parse(response).read("$.id"));

        Author newAuthor = new Author("New author name", 1983);
        newAuthor.getBooks().add(newBook1);
        newAuthor.getBooks().add(newBook2);

        mvc.perform(post("/api/addAuthor")
                .content(mapperWithoutAuthorsRef.writeValueAsString(newAuthor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Customer customer = new Customer("Customer name", "+7-222-222-22-22");

        result = mvc.perform(post("/api/addCustomer")
                .content(mapperWithoutOrdersRef.writeValueAsString(customer))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        response = result.getResponse().getContentAsString();
        Integer customerId = JsonPath.parse(response).read("$.id");
        customer.setId(customerId);

        Date date = new Date();
        String formatted = new SimpleDateFormat("dd.MM.yyyy").format(date);

        Order order1 = new Order(customer, date);
        order1.setCompleted(true);
        order1.setCompleteDate(date);
        order1.getBooks().add(newBook1);

        mvc.perform(post("/api/addOrder")
                .content(mapperWithoutAuthorsAndOrdersRef.writeValueAsString(order1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Order order2 = new Order(customer, date);
        order2.getBooks().add(newBook1);
        order2.getBooks().add(newBook2);

        mvc.perform(post("/api/addOrder")
                .content(mapperWithoutAuthorsAndOrdersRef.writeValueAsString(order2))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/getCustomerById?id=" + customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.getName())))
                .andExpect(jsonPath("$.phone", is(customer.getPhone())))
                .andExpect(jsonPath("$.orders", hasSize(2)))
                .andExpect(jsonPath("$.orders[0].creationDate", is(formatted)))
                .andExpect(jsonPath("$.orders[0].completeDate", is(formatted)))
                .andExpect(jsonPath("$.orders[0].completed", is(true)))
                .andExpect(jsonPath("$.orders[1].creationDate", is(formatted)))
                .andExpect(jsonPath("$.orders[1].completeDate", IsNull.nullValue()))
                .andExpect(jsonPath("$.orders[1].completed", is(false)))
                .andExpect(jsonPath("$.orders[0].books", hasSize(1)))
                .andExpect(jsonPath("$.orders[1].books", hasSize(2)));

        mvc.perform(get("/api/getOrdersByCustomerId?id=" + customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].creationDate", is(formatted)))
                .andExpect(jsonPath("$[0].completeDate", is(formatted)))
                .andExpect(jsonPath("$[0].completed", is(true)))
                .andExpect(jsonPath("$[1].creationDate", is(formatted)))
                .andExpect(jsonPath("$[1].completeDate", IsNull.nullValue()))
                .andExpect(jsonPath("$[1].completed", is(false)))
                .andExpect(jsonPath("$[0].books", hasSize(1)))
                .andExpect(jsonPath("$[1].books", hasSize(2)));
    }
}
