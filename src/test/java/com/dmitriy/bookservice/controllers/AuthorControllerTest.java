package com.dmitriy.bookservice.controllers;

import com.dmitriy.bookservice.model.Author;
import com.dmitriy.bookservice.service.AuthorService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthorController.class)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorService authorService;

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

    private Author newAuthor = new Author("New author name", 1986);

    @Before
    public void setUp() throws JsonProcessingException {
        Author author = new Author("Author name", 1980);
        author.setId(1);
        List<Author> list = Arrays.asList(author);

        newAuthor.setId(2);

        Mockito.when(authorService.findAll()).thenReturn(mapperWithoutBooksRef.writeValueAsString(list));
        Mockito.when(authorService.findByFullName(author.getFullName())).thenReturn(mapperWithoutAuthorsRef.writeValueAsString(list));
        Mockito.when(authorService.findById(author.getId())).thenReturn(mapperWithoutAuthorsRef.writeValueAsString(author));
        Mockito.when(authorService.add(newAuthor)).thenReturn(newAuthor);
    }

    @Test
    public void getAuthors() throws Exception {
        mvc.perform(get("/api/getAuthors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Author name")))
                .andExpect(jsonPath("$[0].birthYear", is(1980)));

        mvc.perform(get("/api/getAuthorsByFullName?fullName=Author name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Author name")))
                .andExpect(jsonPath("$[0].birthYear", is(1980)));

        mvc.perform(get("/api/getAuthorById?id=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Author name")))
                .andExpect(jsonPath("$.birthYear", is(1980)));
    }

    @Test
    public void addAuthor() throws Exception {
        mvc.perform(post("/api/addAuthor")
                .content(mapperWithoutBooksRef.writeValueAsString(newAuthor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));
    }
}
