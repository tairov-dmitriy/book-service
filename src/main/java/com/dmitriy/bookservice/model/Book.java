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
@Table(name = "books")
@JsonFilter("nestedFilter")
public class Book {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Unique ID", name = "id", required = true)
    private int id;

    @Column(name = "name")
    @ApiModelProperty(value = "Book name", name = "name", required = true)
    @NotNull
    @Size(min = 1, max = 256)
    private String name;

    @Column(name = "publicationYear")
    @ApiModelProperty(value = "Year of the publication", name = "publicationYear", required = true)
    @YearConstraint
    private int publicationYear;

    @Column(name = "annotation")
    @ApiModelProperty(value = "Book annotation", name = "annotation", required = true)
    @NotNull
    @Size(min = 1, max = 4096)
    private String annotation;

    @ManyToMany
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JoinTable(
            name = "books_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    @ApiModelProperty(value = "Book authors", name = "authors", required = true)
    private Set<Author> authors = new HashSet<>();

    public Book() {}

    public Book(@NotNull @Size(min = 1, max = 256) String name, @YearConstraint int publicationYear, @NotNull @Size(min = 1, max = 4096) String annotation) {
        this.name = name;
        this.publicationYear = publicationYear;
        this.annotation = annotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Book b = ((Book)obj);
        return id == b.getId() && name.equals(b.getName()) && publicationYear == b.getPublicationYear() &&
               annotation.equals(b.getAnnotation()) && authors.equals(b.getAuthors());
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

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }
}
