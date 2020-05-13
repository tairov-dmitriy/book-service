package com.dmitriy.bookservice.model;

import com.dmitriy.bookservice.validation.YearConstraint;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
@JsonFilter("nestedFilter")
public class Author {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Unique ID", name = "id", required = true)
    private int id;

    @Column(name = "fullName")
    @ApiModelProperty(value = "Author full name", name = "fullName", required = true, example = "Donald Knuth")
    @NotNull
    @Size(min = 1, max = 256)
    private String fullName;

    @Column(name = "birthYear")
    @ApiModelProperty(value = "Year of birth", name = "birthYear", required = true, example = "1938")
    @YearConstraint
    private int birthYear;

    @ManyToMany
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Book.class)
    @JoinTable(
            name = "books_authors",
            joinColumns = @JoinColumn(name = "author_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    @ApiModelProperty(value = "Written books", name = "books", required = true)
    private Set<Book> books = new HashSet<>();

    public Author() {}

    public Author(@NotNull @Size(min = 1, max = 256) String fullName, @YearConstraint int birthYear) {
        this.fullName = fullName;
        this.birthYear = birthYear;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Author a = ((Author)obj);
        return id == a.getId() && fullName.equals(a.getFullName()) && birthYear == a.getBirthYear() && books.equals(a.getBooks());
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }
}
