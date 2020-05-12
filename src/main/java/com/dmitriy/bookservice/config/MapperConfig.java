package com.dmitriy.bookservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MapperConfig {

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
