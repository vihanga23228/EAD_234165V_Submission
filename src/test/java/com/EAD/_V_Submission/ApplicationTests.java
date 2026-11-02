package com.EAD._V_Submission;

import com.EAD._V_Submission.Model.Book;
import com.EAD._V_Submission.Repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void clearBooks() {
        bookRepository.deleteAll();
    }

    @Test
    void createBookReturnsCreatedBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","author":"Robert C. Martin","isbn":"9780132350884","price":4500.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"))
                .andExpect(jsonPath("$.price").value(4500.00));
    }

    @Test
    void listBooksReturnsAllBooks() throws Exception {
        bookRepository.save(new Book("Clean Code", "Robert C. Martin"));
        bookRepository.save(new Book("Effective Java", "Joshua Bloch"));
        bookRepository.save(new Book("Design Patterns", "Erich Gamma"));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getUpdateAndDeleteBook() throws Exception {
        Book book = bookRepository.save(new Book("Old title", "Old author"));

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Old title"));

        mockMvc.perform(put("/api/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New title","author":"New author","isbn":"1234567890","price":1200.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.author").value("New author"))
                .andExpect(jsonPath("$.isbn").value("1234567890"))
                .andExpect(jsonPath("$.price").value(1200.00));

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBookRejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","author":"Robert C. Martin"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookRejectsBlankAuthorAndNegativePrice() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title","author":" "}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title","author":"Valid author","price":-1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingBookReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void swaggerDocumentsCreateSummaryAndResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/books'].post.summary").value("Create a book"))
                .andExpect(jsonPath("$.paths['/api/books'].post.responses['201'].description")
                        .value("Book created"))
                .andExpect(jsonPath("$.paths['/api/books'].post.responses['400'].description")
                        .value("Invalid input"))
                .andExpect(jsonPath("$.paths['/api/books'].post.tags[0]").value("Books"));
    }
}
