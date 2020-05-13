package com.dmitriy.bookservice.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
@JsonFilter("nestedFilter")
public class Customer {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Unique ID", name = "id", required = true)
    private int id;

    @Column(name = "name")
    @ApiModelProperty(value = "Customer name", name = "name", required = true, example = "Ivanov Ivan")
    @NotNull
    @Size(min = 1, max = 256)
    private String name;

    @Column(name = "phone")
    @ApiModelProperty(value = "Customer phone", name = "phone", required = true, example = "+7-111-111-11-11")
    @NotNull
    @Size(min = 1, max = 20)
    @Pattern(regexp = "^\\+?[\\d\\s-]*(\\([\\d\\s-]*\\))?[\\d\\s-]*$")
    private String phone;

    @OneToMany(mappedBy = "customer")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Order.class)
    @ApiModelProperty(hidden = true)
    private Set<Order> orders = new HashSet<>();

    public Customer() {}

    public Customer(@NotNull @Size(min = 1, max = 256) String name,
                    @NotNull @Size(min = 1, max = 20) @Pattern(regexp = "^\\+?[\\d\\s-]*(\\([\\d\\s-]*\\))?[\\d\\s-]*$") String phone) {
        this.name = name;
        this.phone = phone;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Customer c = ((Customer)obj);
        return id == c.getId() && name.equals(c.getName()) && phone.equals(c.getPhone()) && orders.equals(c.getOrders());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }
}
