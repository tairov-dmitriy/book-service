package com.dmitriy.bookservice.model;

import com.dmitriy.bookservice.validation.DateConstraint;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@JsonFilter("nestedFilter")
public class Order {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Unique ID", name = "id", required = true)
    private int id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Customer.class)
    @ApiModelProperty(value = "Customer", name = "customer", required = true)
    @NotNull
    private Customer customer;

    @Column(name = "creationDate")
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone="UTC")
    @ApiModelProperty(value = "Date of the creation order", name = "creationDate", required = true, example = "13.05.2020")
    @NotNull
    @DateConstraint
    private Date creationDate;

    @Column(name = "completeDate")
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone="UTC")
    @ApiModelProperty(value = "Date of the completion order", name = "completeDate", example = "13.05.2020")
    @DateConstraint
    private Date completeDate;

    @Column(name = "completed")
    @ApiModelProperty(value = "Flag of the completion order", name = "completed", required = true, example = "false")
    @NotNull
    private Boolean completed;

    @ManyToMany
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Book.class)
    @JoinTable(
            name = "orders_books",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    @ApiModelProperty(value = "Ordered books", name = "books", required = true)
    private Set<Book> books = new HashSet<>();

    public Order() {}

    public Order(@NotNull Customer customer, @DateConstraint Date creationDate) {
        this.customer = customer;
        this.creationDate = creationDate;
        this.completed = false;
        this.completeDate = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Order o = ((Order)obj);
        return id == o.getId() && customer.equals(o.getCustomer()) && creationDate.equals(o.getCreationDate()) &&
               completeDate.equals(o.getCompleteDate()) && completed.equals(o.getCompleted()) && books.equals(o.getBooks());
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }
}
