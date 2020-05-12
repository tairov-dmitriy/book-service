package com.dmitriy.bookservice.integration;

import com.dmitriy.bookservice.BookserviceApplication;
import com.dmitriy.bookservice.model.Author;
import com.dmitriy.bookservice.repository.AuthorRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
public class AuthorControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AuthorRepository authorRepository;

    @TestConfiguration
    static class AuthorControllerTestContextConfiguration {

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

    @After
    public void resetDb() {
        authorRepository.deleteAll();
    }

    @Test
    public void getAuthors() throws Exception {

        resetDb();

        Author author = new Author("Author name", 1980);
        authorRepository.save(author);

        mvc.perform(get("/api/getAuthors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is(author.getFullName())))
                .andExpect(jsonPath("$[0].birthYear", is(author.getBirthYear())));

        MvcResult result = mvc.perform(get("/api/getAuthorsByFullName?fullName=Author name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is(author.getFullName())))
                .andExpect(jsonPath("$[0].birthYear", is(author.getBirthYear())))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$[0].id");

        mvc.perform(get("/api/getAuthorById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is(author.getFullName())))
                .andExpect(jsonPath("$.birthYear", is(author.getBirthYear())));
    }

    @Test
    public void addAuthor() throws Exception {

        resetDb();

        Author author = new Author("Author name", 1980);
        authorRepository.save(author);

        Author newAuthor = new Author("New author name", 1986);

        MvcResult result = mvc.perform(post("/api/addAuthor")
                .content(mapperWithoutAuthorsRef.writeValueAsString(newAuthor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$.id");

        mvc.perform(get("/api/getAuthorsByFullName?fullName=New author name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is(newAuthor.getFullName())))
                .andExpect(jsonPath("$[0].birthYear", is(newAuthor.getBirthYear())));

        mvc.perform(get("/api/getAuthorById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is(newAuthor.getFullName())))
                .andExpect(jsonPath("$.birthYear", is(newAuthor.getBirthYear())));
    }

    @Test
    public void updateAuthor() throws Exception {

        resetDb();

        Author author = new Author("Author name", 1980);
        author = authorRepository.save(author);

        Author updatedAuthor = new Author("Updated author name", 1986);
        updatedAuthor.setId(author.getId());

        mvc.perform(post("/api/updateAuthor")
                .content(mapperWithoutAuthorsRef.writeValueAsString(updatedAuthor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/getAuthorsByFullName?fullName=Updated author name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is(updatedAuthor.getFullName())))
                .andExpect(jsonPath("$[0].birthYear", is(updatedAuthor.getBirthYear())));

        mvc.perform(get("/api/getAuthorById?id=" + author.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is(updatedAuthor.getFullName())))
                .andExpect(jsonPath("$.birthYear", is(updatedAuthor.getBirthYear())));
    }

    @Test
    public void deleteAuthor() throws Exception {
        resetDb();

        Author author = new Author("Author name", 1980);
        author = authorRepository.save(author);

        mvc.perform(get("/api/deleteAuthor?id=" + author.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/getAuthorsByFullName?fullName=Author name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        try {
            mvc.perform(get("/api/getAuthorById?id=" + author.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            fail("Not existed author was founded");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("Author (id = " + author.getId() + ") not found"));
        }
    }
}
