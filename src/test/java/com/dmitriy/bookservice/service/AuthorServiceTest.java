package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Author;
import com.dmitriy.bookservice.repository.AuthorRepository;
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
public class AuthorServiceTest {

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
        public AuthorService authorService() {
            return new AuthorServiceImpl();
        }
    }

    @Autowired
    private AuthorService authorService;

    @MockBean
    private AuthorRepository authorRepository;

    @Before
    public void setUp() {
        Author author = new Author("Author name", 1980);
        author.setId(1);
        List<Author> list = Arrays.asList(author);

        Mockito.when(authorRepository.findByFullName(author.getFullName())).thenReturn(list);
        Mockito.when(authorRepository.findById(author.getId())).thenReturn(Optional.of(author));
        Mockito.when(authorRepository.findAll()).thenReturn(list);
    }

    @Test
    public void getJSONAuthors() {
        String answer = authorService.findByFullName("Author name");
        assertThat(answer).isEqualTo("[{\"id\":1,\"fullName\":\"Author name\",\"birthYear\":1980,\"books\":[]}]");

        answer = authorService.findById(1);
        assertThat(answer).isEqualTo("{\"id\":1,\"fullName\":\"Author name\",\"birthYear\":1980,\"books\":[]}");

        answer = authorService.findAll();
        assertThat(answer).isEqualTo("[{\"id\":1,\"fullName\":\"Author name\",\"birthYear\":1980}]");
    }
}
