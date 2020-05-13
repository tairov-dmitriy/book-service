package com.dmitriy.bookservice.controllers;

import com.dmitriy.bookservice.model.Identificator;
import com.dmitriy.bookservice.model.Order;
import com.dmitriy.bookservice.service.OrderService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(description = "Order REST APIs", tags = "Orders")
@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "Get order by specified ID", response = Order.class, tags = "Get")
    @GetMapping(value = "/getOrderById", produces = "application/json; charset=UTF-8")
    public String getOrderById(
            @ApiParam(name = "id", required = true, value = "Order ID", example = "1")
            @RequestParam(value = "id") int id) {
        return orderService.findById(id);
    }

    @ApiOperation(value = "Get list of all orders", response = Order.class, responseContainer = "List", tags = "Get")
    @GetMapping(value = "/getOrders", produces = "application/json; charset=UTF-8")
    public String getOrders() {
        return orderService.findAll();
    }

    @ApiOperation(value = "Add new order", response = Identificator.class, tags = "Create")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Order added, return ID of the new order")
    })
    @PostMapping(value = "/addOrder", produces = "application/json; charset=UTF-8")
    public Identificator addOrder(
            @ApiParam(name = "order", required = true,
                    value = "JSON data of the new order. Order ID must be equal 0 or absent. Specified customer and book IDs must exist, " +
                            "other customer and books fields must be null or absent.")
            @RequestBody Order order) {
        return new Identificator(orderService.add(order).getId());
    }

    @ApiOperation(value = "Update order", tags = "Update")
    @PostMapping(value = "/updateOrder", produces = "application/json; charset=UTF-8")
    public void updateOrder(
            @ApiParam(name = "order", required = true,
                    value = "JSON data of the order, identified by existing order ID. Specified customer and book IDs must exist, " +
                            "other customer and books fields must be null or absent.")
            @RequestBody Order order) {
        orderService.update(order);
    }

    @ApiOperation(value = "Delete order", tags = "Delete")
    @GetMapping(value = "/deleteOrder", produces = "application/json; charset=UTF-8")
    public void deleteOrder(
            @ApiParam(name = "id", required = true, value = "ID of existing order", example = "1")
            @RequestParam(value = "id") int id) {
        orderService.delete(id);
    }

    @ApiOperation(value = "Find orders of customer specified by ID", response = Order.class, responseContainer = "List", tags = "Get")
    @GetMapping(value = "/getOrdersByCustomerId", produces = "application/json; charset=UTF-8")
    public String getOrdersByCustomerId(
            @ApiParam(name = "id", required = true, value = "Customer ID", example = "1")
            @RequestParam(value = "id") int id) {
        return orderService.findByCustomerId(id);
    }

    @ApiOperation(value = "Complete order", tags = "Update")
    @GetMapping(value = "/completeOrder", produces = "application/json; charset=UTF-8")
    public void completeOrder(
            @ApiParam(name = "id", required = true, value = "ID of existing order", example = "1")
            @RequestParam(value = "id") int id) {
        orderService.completeById(id);
    }
}
