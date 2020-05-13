package com.dmitriy.bookservice.controllers;

import com.dmitriy.bookservice.model.Customer;
import com.dmitriy.bookservice.model.Identificator;
import com.dmitriy.bookservice.service.CustomerService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Api(description = "Customer REST APIs", tags = "Customers")
@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @ApiOperation(value = "Get customer by specified ID", response = Customer.class, tags = "Get")
    @GetMapping(value = "/getCustomerById", produces = "application/json; charset=UTF-8")
    public String getCustomerById(
            @ApiParam(name = "id", required = true, value = "Customer ID", example = "1")
            @RequestParam(value = "id") int id) {
        return customerService.findById(id);
    }

    @ApiOperation(value = "Get list of customers by specified name", response = Customer.class, responseContainer = "List", tags = "Get")
    @GetMapping(value = "/getCustomersByName", produces = "application/json; charset=UTF-8")
    public String getCustomersByName(
            @ApiParam(name = "name", required = true, value = "Customer name", example = "Ivanov Ivan")
            @RequestParam(value = "name") String name) {
        return customerService.findByName(name);
    }

    @ApiOperation(value = "Get list of all customers", response = Customer.class, responseContainer = "List", tags = "Get")
    @GetMapping(value = "/getCustomers", produces = "application/json; charset=UTF-8")
    public String getCustomers() {
        return customerService.findAll();
    }

    @ApiOperation(value = "Add new customer", response = Identificator.class, tags = "Create")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Customer added, return ID of the new customer")
    })
    @PostMapping(value = "/addCustomer", produces = "application/json; charset=UTF-8")
    public Identificator addCustomer(
            @ApiParam(name = "customer", required = true,
                    value = "JSON data of the new customer. Customer ID must be equal 0 or absent.")
            @RequestBody Customer customer) {
        return new Identificator(customerService.add(customer).getId());
    }

    @ApiOperation(value = "Update customer", tags = "Update")
    @PostMapping(value = "/updateCustomer", produces = "application/json; charset=UTF-8")
    public void updateCustomer(
            @ApiParam(name = "customer", required = true,
                    value = "JSON data of the customer, identified by existing customer ID")
            @RequestBody Customer customer) {
        customerService.update(customer);
    }

    @ApiOperation(value = "Delete customer", tags = "Delete")
    @GetMapping(value = "/deleteCustomer", produces = "application/json; charset=UTF-8")
    public void deleteCustomer(
            @ApiParam(name = "id", required = true, value = "ID of existing customer", example = "1")
            @RequestParam(value = "id") int id) {
        customerService.delete(id);
    }

    // Generate report
    @ApiOperation(value = "Generate a report on books ordered by customers for a specified period", tags = "Report")
    @GetMapping(value = "/generateOrderReport", produces = "application/json; charset=UTF-8")
    public Iterable<Object[]> generateOrderReport(
            @ApiParam(name = "startDate", required = true, value = "Start date", example = "13.05.2020")
            @RequestParam(value = "startDate") String startDate,
            @ApiParam(name = "endDate", required = true, value = "End date", example = "13.05.2020")
            @RequestParam(value = "endDate") String endDate,
            @ApiParam(name = "onlyCompleted", value = "Include only completed orders", example = "true")
            @RequestParam(value = "onlyCompleted", required = false) Boolean onlyCompleted) {

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date start, end;
        try {
            start = format.parse(startDate);
            end = format.parse(endDate);
        } catch (ParseException ex) {
            throw new IllegalStateException("Failed parse specified date: " + ex.getMessage());
        }

        return customerService.reportOrders(start, end, onlyCompleted);
    }
}
