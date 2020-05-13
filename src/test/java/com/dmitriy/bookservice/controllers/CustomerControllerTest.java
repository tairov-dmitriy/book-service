package com.dmitriy.bookservice.controllers;

import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.service.CustomerService;
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
@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CustomerService customerService;

    @TestConfiguration
    static class CustomerControllerTestContextConfiguration {

        @Bean
        @Primary
        ObjectMapper defaultMapper() {
            return new ObjectMapper();
        }

        @Bean("mapperWithoutOrdersRef")
        ObjectMapper mapperWithoutOrdersRef() {
            ObjectMapper mapperWithoutAuthorsRef = new ObjectMapper();
            mapperWithoutAuthorsRef.setFilterProvider(new SimpleFilterProvider().addFilter("nestedFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("orders")));
            return mapperWithoutAuthorsRef;
        }

        @Bean("mapperWithoutAuthorsAndCustomerRef")
        ObjectMapper mapperWithoutAuthorsAndCustomerRef() {
            ObjectMapper mapperWithoutAuthorsAndCustomerRef = new ObjectMapper();
            mapperWithoutAuthorsAndCustomerRef.setFilterProvider(new SimpleFilterProvider().addFilter("nestedFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("authors", "customer")));
            return mapperWithoutAuthorsAndCustomerRef;
        }
    }

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("mapperWithoutOrdersRef")
    private ObjectMapper mapperWithoutOrdersRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsAndCustomerRef")
    private ObjectMapper mapperWithoutAuthorsAndCustomerRef;

    private Customer newCustomer = new Customer("Customer name 2", "+7-222-222-22-22");

    @Before
    public void setUp() throws JsonProcessingException {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        customer.setId(1);
        List<Customer> list = Arrays.asList(customer);

        newCustomer.setId(2);

        Mockito.when(customerService.findAll()).thenReturn(mapperWithoutOrdersRef.writeValueAsString(list));
        Mockito.when(customerService.findByName(customer.getName())).thenReturn(mapperWithoutAuthorsAndCustomerRef.writeValueAsString(list));
        Mockito.when(customerService.findById(customer.getId())).thenReturn(mapperWithoutAuthorsAndCustomerRef.writeValueAsString(customer));
        Mockito.when(customerService.add(newCustomer)).thenReturn(newCustomer);
    }

    @Test
    public void getCustomers() throws Exception {
        mvc.perform(get("/api/getCustomers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Customer name")))
                .andExpect(jsonPath("$[0].phone", is("+7-111-111-11-11")));

        mvc.perform(get("/api/getCustomersByName?name=Customer name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Customer name")))
                .andExpect(jsonPath("$[0].phone", is("+7-111-111-11-11")));

        mvc.perform(get("/api/getCustomerById?id=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Customer name")))
                .andExpect(jsonPath("$.phone", is("+7-111-111-11-11")));
    }

    @Test
    public void addCustomer() throws Exception {
        mvc.perform(post("/api/addCustomer")
                .content(mapperWithoutOrdersRef.writeValueAsString(newCustomer))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));
    }
}
