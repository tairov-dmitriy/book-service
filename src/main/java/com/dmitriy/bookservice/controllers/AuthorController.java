package com.dmitriy.bookservice.controllers;

import com.dmitriy.bookservice.model.Author;
import com.dmitriy.bookservice.model.Identificator;
import com.dmitriy.bookservice.service.AuthorService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(description = "Author REST APIs", tags = "Authors")
@RestController
@RequestMapping("/api")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @ApiOperation(value = "Get author by specified ID", response = Author.class, tags = "Get")
    @GetMapping(value = "/getAuthorById", produces = "application/json; charset=UTF-8")
    public String getAuthorById(
            @ApiParam(name = "id", required = true, value = "Author ID", example = "1")
            @RequestParam(value = "id") int id) {
        return authorService.findById(id);
    }

    @ApiOperation(value = "Get list of authors by specified full name", response = Author.class, responseContainer = "List", tags = "Get")
    @GetMapping(value = "/getAuthorsByFullName", produces = "application/json; charset=UTF-8")
    public String getAuthorsByFullName(
            @ApiParam(name = "fullName", required = true, value = "Author full name", example = "Donald Knuth")
            @RequestParam(value = "fullName") String fullName) {
        return authorService.findByFullName(fullName);
    }

    @ApiOperation(value = "Get list of all authors", response = Author.class, responseContainer = "List", tags = "Get")
    @GetMapping(value = "/getAuthors", produces = "application/json; charset=UTF-8")
    public String getAuthors() {
        return authorService.findAll();
    }

    @ApiOperation(value = "Add new author", response = Identificator.class, tags = "Create")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Author added, return ID of the new author")
    })
    @PostMapping(value = "/addAuthor", produces = "application/json; charset=UTF-8")
    public Identificator addAuthor(
            @ApiParam(name = "author", required = true,
                    value = "JSON data of the new author. Author ID must be equal 0 or absent. Specified book IDs must exist, " +
                            "field \"authors\" of book must be null or absent, other book fields are ignored.")
            @RequestBody Author author) {
        return new Identificator(authorService.add(author).getId());
    }

    @ApiOperation(value = "Update author", tags = "Update")
    @PostMapping(value = "/updateAuthor", produces = "application/json; charset=UTF-8")
    public void updateAuthor(
            @ApiParam(name = "author", required = true,
                    value = "JSON data of the author, identified by existing author ID. Specified book IDs must exist, " +
                            "field \"authors\" of book must be null or absent, other book fields are ignored.")
            @RequestBody Author author) {
        authorService.update(author);
    }

    @ApiOperation(value = "Delete author", tags = "Delete")
    @GetMapping(value = "/deleteAuthor", produces = "application/json; charset=UTF-8")
    public void deleteAuthor(
            @ApiParam(name = "id", required = true, value = "ID of existing author", example = "1")
            @RequestParam(value = "id") int id) {
        authorService.delete(id);
    }
}
