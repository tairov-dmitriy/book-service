package com.dmitriy.bookservice.repository;

import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.model.Order;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
public class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void getOrder() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        Order order = new Order(customer, new Date());
        entityManager.persist(customer);
        entityManager.persist(order);
        entityManager.flush();

        Optional<Order> found = orderRepository.findById(order.getId());
        assertTrue(found.isPresent());
        assertThat(found.get().getCompleteDate()).isEqualTo(order.getCompleteDate());
        assertThat(found.get().getCreationDate()).isEqualTo(order.getCreationDate());
        assertThat(found.get().getCompleted()).isEqualTo(order.getCompleted());
        assertThat(found.get().getCustomer()).isEqualTo(order.getCustomer());

        List<Order> found2 = Lists.newArrayList(orderRepository.findAll());
        assertEquals(found2.size(), 1);
        assertThat(found2.get(0).getCompleteDate()).isEqualTo(order.getCompleteDate());
        assertThat(found2.get(0).getCreationDate()).isEqualTo(order.getCreationDate());
        assertThat(found2.get(0).getCompleted()).isEqualTo(order.getCompleted());
        assertThat(found2.get(0).getCustomer()).isEqualTo(order.getCustomer());

        List<Order> found3 = orderRepository.findByCustomerId(customer.getId());
        assertEquals(found3.size(), 1);
        assertThat(found3.get(0).getCompleteDate()).isEqualTo(order.getCompleteDate());
        assertThat(found3.get(0).getCreationDate()).isEqualTo(order.getCreationDate());
        assertThat(found3.get(0).getCompleted()).isEqualTo(order.getCompleted());
        assertThat(found3.get(0).getCustomer()).isEqualTo(order.getCustomer());
    }

    @Test
    public void addOrder() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        entityManager.persist(customer);
        entityManager.flush();

        Order order = new Order(customer, new Date());

        Order saved = orderRepository.save(order);
        assertThat(saved).isEqualTo(order);

        Optional<Order> found = orderRepository.findById(order.getId());
        assertTrue(found.isPresent());
        assertThat(found.get().getCompleteDate()).isEqualTo(order.getCompleteDate());
        assertThat(found.get().getCreationDate()).isEqualTo(order.getCreationDate());
        assertThat(found.get().getCompleted()).isEqualTo(order.getCompleted());
        assertThat(found.get().getCustomer()).isEqualTo(order.getCustomer());
    }

    @Test
    public void updateOrder() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        entityManager.persist(customer);
        entityManager.flush();

        Date date = new Date();
        date.setTime(10000);
        Order order = new Order(customer, date);

        orderRepository.save(order);

        Order updatedOrder = new Order(customer, date);
        updatedOrder.setCompleted(true);
        updatedOrder.setCompleteDate(new Date());
        updatedOrder.setId(order.getId());

        orderRepository.save(updatedOrder);

        Optional<Order> found = orderRepository.findById(order.getId());
        assertTrue(found.isPresent());
        assertThat(found.get().getCompleteDate()).isEqualTo(updatedOrder.getCompleteDate());
        assertThat(found.get().getCreationDate()).isEqualTo(updatedOrder.getCreationDate());
        assertThat(found.get().getCompleted()).isEqualTo(updatedOrder.getCompleted());
        assertThat(found.get().getCustomer()).isEqualTo(updatedOrder.getCustomer());
    }

    @Test
    public void deleteOrder() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        entityManager.persist(customer);
        entityManager.flush();

        Order order = new Order(customer, new Date());

        orderRepository.save(order);

        orderRepository.deleteById(order.getId());

        Optional<Order> found = orderRepository.findById(order.getId());
        assertFalse(found.isPresent());
        List<Order> found2 = Lists.newArrayList(orderRepository.findAll());
        assertEquals(found2.size(), 0);
    }

    @Test
    public void validation() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        entityManager.persist(customer);
        entityManager.flush();

        Date date = new Date();
        date.setTime(5000000000000L);
        Order order = new Order(customer, null);

        try {
            orderRepository.save(order);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        order = new Order(customer, date);

        try {
            orderRepository.save(order);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        order = new Order(customer, new Date());
        order.setCompleteDate(date);

        try {
            orderRepository.save(order);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}
    }
}
