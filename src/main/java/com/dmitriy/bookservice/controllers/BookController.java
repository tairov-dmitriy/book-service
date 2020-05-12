package com.dmitriy.bookservice.controllers;

import com.dmitriy.bookservice.model.Book;
import com.dmitriy.bookservice.model.Identificator;
import com.dmitriy.bookservice.service.BookService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(description = "Book REST APIs", tags = "Books")
@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    private BookService bookService;

    @ApiOperation(value = "Get book by specified ID", response = Book.class, tags = "Get")
    @GetMapping(value = "/getBookById", produces = "application/json; charset=UTF-8")
    public String getBookById(
            @ApiParam(name = "id", required = true, value = "Book ID", example = "1")
            @RequestParam(value = "id") int id) {
        return bookService.findById(id);
    }

    @ApiOperation(value = "Get list of books by specified name", response = Book.class, responseContainer = "List", tags = "Get")
    @GetMapping(value = "/getBooksByName", produces = "application/json; charset=UTF-8")
    public String getBooksByName(
            @ApiParam(name = "name", required = true, value = "Book name", example = "The Art of Computer Programming")
            @RequestParam(value = "name") String name) {
        return bookService.findByName(name);
    }

    @ApiOperation(value = "Get list of all books", response = Book.class, responseContainer = "List", tags = "Get")
    @GetMapping(value = "/getBooks", produces = "application/json; charset=UTF-8")
    public String getBooks() {
        return bookService.findAll();
    }

    @ApiOperation(value = "Add new book", response = Identificator.class, tags = "Create")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Book added, return ID of the new book")
    })
    @PostMapping(value = "/addBook", produces = "application/json; charset=UTF-8")
    public Identificator addBook(
            @ApiParam(name = "book", required = true,
                    value = "JSON data of the new book. Book ID must be equal 0 or absent. Specified author IDs must exist, " +
                            "field \"books\" of author must be null or absent, other author fields are ignored.")
            @RequestBody Book book) {
        return new Identificator(bookService.add(book).getId());
    }

    @ApiOperation(value = "Update book", tags = "Update")
    @PostMapping(value = "/updateBook", produces = "application/json; charset=UTF-8")
    public void updateBook(
            @ApiParam(name = "book", required = true,
                    value = "JSON data of the book, identified by existing book ID. Specified author IDs must exist, " +
                            "field \"books\" of author must be null or absent, other author fields are ignored.")
            @RequestBody Book book) {
        bookService.update(book);
    }

    @ApiOperation(value = "Delete book", tags = "Delete")
    @GetMapping(value = "/deleteBook", produces = "application/json; charset=UTF-8")
    public void deleteBook(
            @ApiParam(name = "id", required = true, value = "ID of existing book", example = "1")
            @RequestParam(value = "id") int id) {
        bookService.delete(id);
    }
}
