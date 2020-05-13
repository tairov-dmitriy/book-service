package com.dmitriy.bookservice.repository;

import com.dmitriy.bookservice.model.Customer;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
public class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void getCustomer() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");
        entityManager.persist(customer);
        entityManager.flush();

        List<Customer> found = customerRepository.findByName(customer.getName());
        assertThat(found.get(0).getName()).isEqualTo(customer.getName());
        assertThat(found.get(0).getPhone()).isEqualTo(customer.getPhone());

        Optional<Customer> found2 = customerRepository.findById(customer.getId());
        assertTrue(found2.isPresent());
        assertThat(found2.get().getName()).isEqualTo(customer.getName());
        assertThat(found2.get().getPhone()).isEqualTo(customer.getPhone());

        List<Customer> found3 = Lists.newArrayList(customerRepository.findAll());
        assertEquals(found3.size(), 1);
        assertThat(found3.get(0).getName()).isEqualTo(customer.getName());
        assertThat(found3.get(0).getPhone()).isEqualTo(customer.getPhone());
    }

    @Test
    public void addCustomer() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");

        Customer saved = customerRepository.save(customer);
        assertThat(saved).isEqualTo(customer);

        List<Customer> found = customerRepository.findByName("Customer name");
        assertThat(found.get(0).getName()).isEqualTo(customer.getName());
        assertThat(found.get(0).getPhone()).isEqualTo(customer.getPhone());
    }

    @Test
    public void updateCustomer() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");

        customerRepository.save(customer);

        Customer updatedCustomer = new Customer("Customer name 2", "+7-222-222-22-22");
        updatedCustomer.setId(customer.getId());

        customerRepository.save(updatedCustomer);

        List<Customer> found = customerRepository.findByName("Customer name");
        assertEquals(found.size(), 0);
        List<Customer> found2 = customerRepository.findByName("Customer name 2");
        assertEquals(found2.size(), 1);
        assertThat(found2.get(0).getName()).isEqualTo(updatedCustomer.getName());
        assertThat(found2.get(0).getPhone()).isEqualTo(updatedCustomer.getPhone());
    }

    @Test
    public void deleteCustomer() {
        Customer customer = new Customer("Customer name", "+7-111-111-11-11");

        customerRepository.save(customer);

        customerRepository.deleteById(customer.getId());

        List<Customer> found = customerRepository.findByName("Customer name");
        assertEquals(found.size(), 0);
        Optional<Customer> found2 = customerRepository.findById(customer.getId());
        assertFalse(found2.isPresent());
        List<Customer> found3 = Lists.newArrayList(customerRepository.findAll());
        assertEquals(found3.size(), 0);
    }

    @Test
    public void validation() {
        Customer customer = new Customer("", "+7-111-111-11-11");

        try {
            customerRepository.save(customer);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        customer = new Customer(null, "+7-111-111-11-11");

        try {
            customerRepository.save(customer);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        customer = new Customer("Customer name", "");

        try {
            customerRepository.save(customer);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        customer = new Customer("Customer name", null);

        try {
            customerRepository.save(customer);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}

        customer = new Customer("Customer name", "+7-111-1t1-11-11");

        try {
            customerRepository.save(customer);
            fail("Validation failed");
        } catch (ConstraintViolationException ex) {}
    }
}
