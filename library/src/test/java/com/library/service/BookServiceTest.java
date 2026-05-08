package com.library.service;

import com.org.library.dto.request.BookRequest;
import com.org.library.dto.response.BookResponse;
import com.org.library.entity.Author;
import com.org.library.entity.Book;
import com.org.library.exception.BadRequestException;
import com.org.library.exception.DuplicateResourceException;
import com.org.library.exception.ResourceNotFoundException;
import com.org.library.repository.BookRepository;
import com.org.library.service.AuthorService;
import com.org.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorService authorService;

    @InjectMocks
    private BookService bookService;

    // ── Helper data ───────────────────────────────────────────────────────────

    private Author author() {
        return Author.builder()
                .id(1L)
                .name("Stephen King")
                .email("stephen@example.com")
                .books(new ArrayList<>())
                .build();
    }

    private Book availableBook(Author author) {
        return Book.builder()
                .id(1L)
                .title("Harry Potter")
                .isbn("978-123")
                .available(true)
                .author(author)
                .build();
    }

    private Book unavailableBook(Author author) {
        return Book.builder()
                .id(1L)
                .title("Harry Potter")
                .isbn("978-123")
                .available(false)
                .author(author)
                .build();
    }

    private BookRequest bookRequest() {
        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter");
        request.setIsbn("978-123");
        request.setAuthorId(1L);
        return request;
    }

    // ── createBook ────────────────────────────────────────────────────────────

    @Test
    void createBook_success() {
        Author author = author();
        BookRequest request = bookRequest();

        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(false);
        when(authorService.findAuthorOrThrow(1L)).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        BookResponse response = bookService.createBook(request);

        assertEquals("Harry Potter", response.getTitle());
        assertEquals("978-123", response.getIsbn());
        assertTrue(response.getAvailable());
        assertEquals(1L, response.getAuthorId());
    }

    @Test
    void createBook_duplicateIsbn_throwsException() {
        BookRequest request = bookRequest();

        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> bookService.createBook(request));
    }

    @Test
    void createBook_authorNotFound_throwsException() {
        BookRequest request = bookRequest();

        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(false);
        when(authorService.findAuthorOrThrow(1L))
                .thenThrow(new ResourceNotFoundException("Author", 1L));

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.createBook(request));
    }

    // ── getAllBooks ────────────────────────────────────────────────────────────

    @Test
    void getAllBooks_noFilter_returnsAll() {
        Author author = author();
        Book b1 = availableBook(author);
        Book b2 = unavailableBook(author);

        when(bookRepository.findAll()).thenReturn(List.of(b1, b2));

        List<BookResponse> responses = bookService.getAllBooks(null);

        assertEquals(2, responses.size());
    }

    @Test
    void getAllBooks_filterAvailable_returnsOnlyAvailable() {
        Author author = author();
        Book b1 = availableBook(author);

        when(bookRepository.findByAvailableTrue()).thenReturn(List.of(b1));

        List<BookResponse> responses = bookService.getAllBooks(true);

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getAvailable());
    }

    @Test
    void getAllBooks_emptyList() {
        when(bookRepository.findAll()).thenReturn(new ArrayList<>());

        List<BookResponse> responses = bookService.getAllBooks(null);

        assertEquals(0, responses.size());
    }

    // ── getBookById ───────────────────────────────────────────────────────────

    @Test
    void getBookById_success() {
        Author author = author();
        Book book = availableBook(author);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.getBookById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Harry Potter", response.getTitle());
    }

    @Test
    void getBookById_notFound_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.getBookById(99L));
    }

    // ── updateBook ────────────────────────────────────────────────────────────

    @Test
    void updateBook_success() {
        Author author = author();
        Book book = availableBook(author);

        BookRequest request = new BookRequest();
        request.setTitle("New Title");
        request.setIsbn("978-999");
        request.setAuthorId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn("978-999")).thenReturn(Optional.empty());
        when(authorService.findAuthorOrThrow(1L)).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        BookResponse response = bookService.updateBook(1L, request);

        assertEquals("New Title", response.getTitle());
        assertEquals("978-999", response.getIsbn());
    }

    @Test
    void updateBook_sameIsbn_success() {
        Author author = author();
        Book book = availableBook(author);

        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter");
        request.setIsbn("978-123");
        request.setAuthorId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn("978-123")).thenReturn(Optional.of(book));
        when(authorService.findAuthorOrThrow(1L)).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        BookResponse response = bookService.updateBook(1L, request);

        assertEquals("978-123", response.getIsbn());
    }

    @Test
    void updateBook_isbnTakenByOther_throwsException() {
        Author author = author();
        Book book = availableBook(author);

        Book otherBook = Book.builder()
                .id(2L)
                .isbn("978-999")
                .author(author)
                .build();

        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter");
        request.setIsbn("978-999");
        request.setAuthorId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn("978-999")).thenReturn(Optional.of(otherBook));

        assertThrows(DuplicateResourceException.class,
                () -> bookService.updateBook(1L, request));
    }

    @Test
    void updateBook_notFound_throwsException() {
        BookRequest request = bookRequest();

        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.updateBook(99L, request));
    }

    // ── deleteBook ────────────────────────────────────────────────────────────

    @Test
    void deleteBook_success() {
        Author author = author();
        Book book = availableBook(author);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertDoesNotThrow(() -> bookService.deleteBook(1L));

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_currentlyBorrowed_throwsException() {
        Author author = author();
        Book book = unavailableBook(author);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(BadRequestException.class,
                () -> bookService.deleteBook(1L));

        verify(bookRepository, never()).delete(any());
    }

    @Test
    void deleteBook_notFound_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.deleteBook(99L));
    }
}