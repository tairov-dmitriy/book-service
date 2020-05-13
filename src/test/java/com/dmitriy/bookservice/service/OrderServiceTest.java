package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.model.Order;
import com.dmitriy.bookservice.repository.OrderRepository;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class OrderServiceTest {

    @TestConfiguration
    static class OrderServiceImplTestContextConfiguration {

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

        @Bean
        public OrderService orderService() {
            return new OrderServiceImpl();
        }
    }

    @Autowired
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @Before
    public void setUp() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        Order order = new Order(customer, new Date());
        order.setId(1);
        List<Order> list = Arrays.asList(order);

        Mockito.when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.findAll()).thenReturn(list);
    }

    @Test
    public void getJSONOrders() {
        String answer = orderService.findById(1);
        assertThat(answer).isEqualTo("{\"id\":1,\"customer\":{\"id\":0,\"name\":\"Customer name\",\"phone\":\"+7-111-111-11-11\"},\"creationDate\":\"13.05.2020\",\"completeDate\":null,\"completed\":false,\"books\":[]}");

        answer = orderService.findAll();
        assertThat(answer).isEqualTo("[{\"id\":1,\"customer\":{\"id\":0,\"name\":\"Customer name\",\"phone\":\"+7-111-111-11-11\"},\"creationDate\":\"13.05.2020\",\"completeDate\":null,\"completed\":false}]");
    }
}
