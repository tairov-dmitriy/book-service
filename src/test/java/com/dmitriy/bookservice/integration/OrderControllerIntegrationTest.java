package com.dmitriy.bookservice.integration;

import com.dmitriy.bookservice.BookserviceApplication;
import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.model.Order;
import com.dmitriy.bookservice.repository.CustomerRepository;
import com.dmitriy.bookservice.repository.OrderRepository;
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
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @TestConfiguration
    static class OrderControllerTestContextConfiguration {

        @Bean
        @Primary
        ObjectMapper defaultMapper() {
            return new ObjectMapper();
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
    @Qualifier("mapperWithoutAuthorsAndOrdersRef")
    private ObjectMapper mapperWithoutAuthorsAndOrdersRef;

    private Date date = new Date();
    private String formatted = new SimpleDateFormat("dd.MM.yyyy").format(date);

    @After
    public void resetDb() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    public void getOrders() throws Exception {

        resetDb();

        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        Order order = new Order(customer, date);
        customerRepository.save(customer);
        orderRepository.save(order);

        MvcResult result = mvc.perform(get("/api/getOrders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].creationDate", is(formatted)))
                .andExpect(jsonPath("$[0].completeDate", IsNull.nullValue()))
                .andExpect(jsonPath("$[0].completed", is(false)))
                .andExpect(jsonPath("$[0].customer.name", is("Customer name")))
                .andExpect(jsonPath("$[0].customer.phone", is("+7-111-111-11-11")))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer customerId = JsonPath.parse(response).read("$[0].customer.id");

        result = mvc.perform(get("/api/getOrdersByCustomerId?id=" + customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].creationDate", is(formatted)))
                .andExpect(jsonPath("$[0].completeDate", IsNull.nullValue()))
                .andExpect(jsonPath("$[0].completed", is(false)))
                .andReturn();

        response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$[0].id");

        mvc.perform(get("/api/getOrderById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creationDate", is(formatted)))
                .andExpect(jsonPath("$.completeDate",IsNull.nullValue()))
                .andExpect(jsonPath("$.completed", is(false)))
                .andExpect(jsonPath("$.customer.name", is("Customer name")))
                .andExpect(jsonPath("$.customer.phone", is("+7-111-111-11-11")));
    }

    @Test
    public void addOrder() throws Exception {

        resetDb();

        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        Customer customer2 = new Customer("Customer name 2", "+7-222-222-22-22");
        Order order = new Order(customer, date);
        customerRepository.save(customer);
        customerRepository.save(customer2);
        orderRepository.save(order);

        Order newOrder = new Order(customer2, date);
        newOrder.setCompleteDate(date);
        newOrder.setCompleted(true);

        MvcResult result = mvc.perform(post("/api/addOrder")
                .content(mapperWithoutAuthorsAndOrdersRef.writeValueAsString(newOrder))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$.id");

        result = mvc.perform(get("/api/getOrderById?id=" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creationDate", is(formatted)))
                .andExpect(jsonPath("$.completeDate",is(formatted)))
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.customer.name", is(customer2.getName())))
                .andExpect(jsonPath("$.customer.phone", is(customer2.getPhone())))
                .andReturn();

        response = result.getResponse().getContentAsString();
        Integer customerId = JsonPath.parse(response).read("$.customer.id");

        mvc.perform(get("/api/getOrdersByCustomerId?id=" + customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].creationDate", is(formatted)))
                .andExpect(jsonPath("$[0].completeDate",is(formatted)))
                .andExpect(jsonPath("$[0].completed", is(true)));
    }

    @Test
    public void updateOrder() throws Exception {

        resetDb();

        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        Order order = new Order(customer, date);
        customerRepository.save(customer);
        orderRepository.save(order);

        Order updatedOrder = new Order(customer, date);
        updatedOrder.setCompleteDate(date);
        updatedOrder.setCompleted(true);
        updatedOrder.setId(order.getId());

        mvc.perform(post("/api/updateOrder")
                .content(mapperWithoutAuthorsAndOrdersRef.writeValueAsString(updatedOrder))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(get("/api/getOrderById?id=" + order.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creationDate", is(formatted)))
                .andExpect(jsonPath("$.completeDate",is(formatted)))
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.customer.name", is(customer.getName())))
                .andExpect(jsonPath("$.customer.phone", is(customer.getPhone())))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer customerId = JsonPath.parse(response).read("$.customer.id");

        mvc.perform(get("/api/getOrdersByCustomerId?id=" + customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].creationDate", is(formatted)))
                .andExpect(jsonPath("$[0].completeDate",is(formatted)))
                .andExpect(jsonPath("$[0].completed", is(true)));
    }

    @Test
    public void deleteOrder() throws Exception {
        resetDb();

        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        Order order = new Order(customer, date);
        customerRepository.save(customer);
        orderRepository.save(order);

        mvc.perform(get("/api/deleteOrder?id=" + order.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        try {
            mvc.perform(get("/api/getOrderById?id=" + order.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            fail("Not existed order was founded");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("Order (id = " + order.getId() + ") not found"));
        }
    }
}
