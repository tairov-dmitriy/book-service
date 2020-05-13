package com.dmitriy.bookservice.service;

import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.repository.CustomerRepository;
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
public class CustomerServiceTest {

    @TestConfiguration
    static class CustomerServiceImplTestContextConfiguration {

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

        @Bean
        public CustomerService customerService() {
            return new CustomerServiceImpl();
        }
    }

    @Autowired
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;

    @Before
    public void setUp() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        customer.setId(1);
        List<Customer> list = Arrays.asList(customer);

        Mockito.when(customerRepository.findByName(customer.getName())).thenReturn(list);
        Mockito.when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Mockito.when(customerRepository.findAll()).thenReturn(list);
    }

    @Test
    public void getJSONCustomers() {
        String answer = customerService.findByName("Customer name");
        assertThat(answer).isEqualTo("[{\"id\":1,\"name\":\"Customer name\",\"phone\":\"+7-111-111-11-11\",\"orders\":[]}]");

        answer = customerService.findById(1);
        assertThat(answer).isEqualTo("{\"id\":1,\"name\":\"Customer name\",\"phone\":\"+7-111-111-11-11\",\"orders\":[]}");

        answer = customerService.findAll();
        assertThat(answer).isEqualTo("[{\"id\":1,\"name\":\"Customer name\",\"phone\":\"+7-111-111-11-11\"}]");
    }
}
