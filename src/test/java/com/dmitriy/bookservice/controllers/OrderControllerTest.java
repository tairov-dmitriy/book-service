package com.dmitriy.bookservice.controllers;

import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.model.Order;
import com.dmitriy.bookservice.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.hamcrest.core.IsNull;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrderService orderService;

    @TestConfiguration
    static class OrderControllerTestContextConfiguration {

        @Bean
        @Primary
        ObjectMapper defaultMapper() {
            return new ObjectMapper();
        }

        @Bean("mapperWithoutBooksAndOrdersRef")
        ObjectMapper mapperWithoutBooksAndOrdersRef() {
            ObjectMapper mapperWithoutBooksAndOrdersRef = new ObjectMapper();
            mapperWithoutBooksAndOrdersRef.setFilterProvider(new SimpleFilterProvider().addFilter("nestedFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("books", "orders")));
            return mapperWithoutBooksAndOrdersRef;
        }

        @Bean("mapperWithoutAuthorsAndOrdersRef")
        ObjectMapper mapperWithoutAuthorsAndOrdersRef() {
            ObjectMapper mapperWithoutAuthorsAndOrdersRef = new ObjectMapper();
            mapperWithoutAuthorsAndOrdersRef.setFilterProvider(new SimpleFilterProvider().addFilter("nestedFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("authors", "orders")));
            return mapperWithoutAuthorsAndOrdersRef;
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
    @Qualifier("mapperWithoutBooksAndOrdersRef")
    private ObjectMapper mapperWithoutBooksAndOrdersRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsAndOrdersRef")
    private ObjectMapper mapperWithoutAuthorsAndOrdersRef;

    @Autowired
    @Qualifier("mapperWithoutAuthorsAndCustomerRef")
    private ObjectMapper mapperWithoutAuthorsAndCustomerRef;

    private Date date = new Date();
    private String formatted = new SimpleDateFormat("dd.MM.yyyy").format(date);
    private Customer customer = new Customer("Customer name", "+7-111-111-11-11");

    @Before
    public void setUp() throws JsonProcessingException {
        Order order = new Order(customer, date);
        customer.setId(1);
        order.setId(1);
        List<Order> list = Arrays.asList(order);

        Mockito.when(orderService.findAll()).thenReturn(mapperWithoutBooksAndOrdersRef.writeValueAsString(list));
        Mockito.when(orderService.findByCustomerId(customer.getId())).thenReturn(mapperWithoutAuthorsAndCustomerRef.writeValueAsString(list));
        Mockito.when(orderService.findById(order.getId())).thenReturn(mapperWithoutAuthorsAndOrdersRef.writeValueAsString(order));
    }

    @Test
    public void getOrders() throws Exception {
        mvc.perform(get("/api/getOrders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].creationDate", is(formatted)))
                .andExpect(jsonPath("$[0].completeDate", IsNull.nullValue()))
                .andExpect(jsonPath("$[0].completed", is(false)))
                .andExpect(jsonPath("$[0].customer.name", is("Customer name")))
                .andExpect(jsonPath("$[0].customer.phone", is("+7-111-111-11-11")));

        mvc.perform(get("/api/getOrdersByCustomerId?id=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].creationDate", is(formatted)))
                .andExpect(jsonPath("$[0].completeDate", IsNull.nullValue()))
                .andExpect(jsonPath("$[0].completed", is(false)));

        mvc.perform(get("/api/getOrderById?id=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creationDate", is(formatted)))
                .andExpect(jsonPath("$.completeDate",IsNull.nullValue()))
                .andExpect(jsonPath("$.completed", is(false)))
                .andExpect(jsonPath("$.customer.name", is("Customer name")))
                .andExpect(jsonPath("$.customer.phone", is("+7-111-111-11-11")));
    }
}
