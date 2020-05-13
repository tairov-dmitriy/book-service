package com.dmitriy.bookservice.integration;

import com.dmitriy.bookservice.BookserviceApplication;
import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.repository.CustomerRepository;
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
public class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CustomerRepository customerRepository;

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
    }

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("mapperWithoutOrdersRef")
    private ObjectMapper mapperWithoutOrdersRef;

    @After
    public void resetDb() {
        customerRepository.deleteAll();
    }

    @Test
    public void getCustomers() throws Exception {

        resetDb();

        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        customerRepository.save(customer);

        mvc.perform(get("/api/getCustomers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(customer.getName())))
                .andExpect(jsonPath("$[0].phone", is(customer.getPhone())));

        MvcResult result = mvc.perform(get("/api/getCustomersByName?name=Customer name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(customer.getName())))
                .andExpect(jsonPath("$[0].phone", is(customer.getPhone())))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$[0].id");

        mvc.perform(get("/api/getCustomerById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.getName())))
                .andExpect(jsonPath("$.phone", is(customer.getPhone())));
    }

    @Test
    public void addCustomer() throws Exception {

        resetDb();

        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        customerRepository.save(customer);

        Customer newCustomer = new Customer("New customer name", "+7-222-222-22-22");

        MvcResult result = mvc.perform(post("/api/addCustomer")
                .content(mapperWithoutOrdersRef.writeValueAsString(newCustomer))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$.id");

        mvc.perform(get("/api/getCustomersByName?name=New customer name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(newCustomer.getName())))
                .andExpect(jsonPath("$[0].phone", is(newCustomer.getPhone())));

        mvc.perform(get("/api/getCustomerById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(newCustomer.getName())))
                .andExpect(jsonPath("$.phone", is(newCustomer.getPhone())));
    }

    @Test
    public void updateCustomer() throws Exception {

        resetDb();

        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        customerRepository.save(customer);

        Customer updatedCustomer = new Customer("Updated customer name", "+7-222-222-22-22");
        updatedCustomer.setId(customer.getId());

        mvc.perform(post("/api/updateCustomer")
                .content(mapperWithoutOrdersRef.writeValueAsString(updatedCustomer))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/getCustomersByName?name=Updated customer name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(updatedCustomer.getName())))
                .andExpect(jsonPath("$[0].phone", is(updatedCustomer.getPhone())));

        mvc.perform(get("/api/getCustomerById?id=" + customer.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updatedCustomer.getName())))
                .andExpect(jsonPath("$.phone", is(updatedCustomer.getPhone())));
    }

    @Test
    public void deleteCustomer() throws Exception {
        resetDb();

        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        customerRepository.save(customer);

        mvc.perform(get("/api/deleteCustomer?id=" + customer.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/getCustomersByName?name=Customer name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        try {
            mvc.perform(get("/api/getCustomerById?id=" + customer.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            fail("Not existed customer was founded");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("Customer (id = " + customer.getId() + ") not found"));
        }
    }
}
