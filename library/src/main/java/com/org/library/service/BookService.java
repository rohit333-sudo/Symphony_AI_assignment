package com.org.library.service;

import com.org.library.dto.request.BookRequest;
import com.org.library.dto.response.BookResponse;
import com.org.library.entity.Author;
import com.org.library.entity.Book;
import com.org.library.exception.BadRequestException;
import com.org.library.exception.DuplicateResourceException;
import com.org.library.exception.ResourceNotFoundException;
import com.org.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository   bookRepository;
    private final AuthorService    authorService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public BookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "Book with ISBN '" + request.getIsbn() + "' already exists");
        }

        Author author = authorService.findAuthorOrThrow(request.getAuthorId());

        Book book = Book.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .available(true)
                .author(author)
                .build();

        return toResponse(bookRepository.save(book));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<BookResponse> getAllBooks(Boolean available) {
        List<Book> books = (available != null && available)
                ? bookRepository.findByAvailableTrue()
                : bookRepository.findAll();

        return books.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BookResponse getBookById(Long id) {
        return toResponse(findBookOrThrow(id));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBookOrThrow(id);

        // Guard: new ISBN must not belong to a DIFFERENT book
        boolean isbnTakenByOther = bookRepository.findByIsbn(request.getIsbn())
                .map(existing -> !existing.getId().equals(id))
                .orElse(false);

        if (isbnTakenByOther) {
            throw new DuplicateResourceException("ISBN '" + request.getIsbn() + "' is already in use");
        }
        Author author = authorService.findAuthorOrThrow(request.getAuthorId());

        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setAuthor(author);

        return toResponse(bookRepository.save(book));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);

        // Prevent deleting a book that is currently borrowed
        if (!book.getAvailable()) {
            throw new BadRequestException(
                    "Cannot delete book with id " + id + " — it is currently borrowed");
        }

        bookRepository.delete(book);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .available(book.getAvailable())
                .authorId(book.getAuthor().getId())
                .authorName(book.getAuthor().getName())
                .build();
    }
}
