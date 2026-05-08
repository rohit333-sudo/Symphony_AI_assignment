package com.library.service;

import com.org.library.dto.request.AuthorRequest;
import com.org.library.dto.response.AuthorResponse;
import com.org.library.entity.Author;
import com.org.library.exception.DuplicateResourceException;
import com.org.library.exception.ResourceNotFoundException;
import com.org.library.repository.AuthorRepository;
import com.org.library.service.AuthorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorService tests")
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author author;
    private AuthorRequest request;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .id(1L)
                .name("George Orwell")
                .email("orwell@books.com")
                .books(new ArrayList<>())
                .build();

        request = new AuthorRequest();
        request.setName("George Orwell");
        request.setEmail("orwell@books.com");
    }

    // ── createAuthor ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createAuthor — success")
    void createAuthor_success() {
        when(authorRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        AuthorResponse response = authorService.createAuthor(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("George Orwell");
        assertThat(response.getEmail()).isEqualTo("orwell@books.com");
        assertThat(response.getTotalBooks()).isZero();

        verify(authorRepository).save(any(Author.class));
    }

    @Test
    @DisplayName("createAuthor — throws DuplicateResourceException when email already exists")
    void createAuthor_duplicateEmail_throws() {
        when(authorRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authorService.createAuthor(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("orwell@books.com");

        verify(authorRepository, never()).save(any());
    }

    // ── getAuthorById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAuthorById — returns author when found")
    void getAuthorById_found() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        AuthorResponse response = authorService.getAuthorById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("George Orwell");
    }

    @Test
    @DisplayName("getAuthorById — throws ResourceNotFoundException when not found")
    void getAuthorById_notFound_throws() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.getAuthorById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getAllAuthors ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllAuthors — returns list of all authors")
    void getAllAuthors_returnsList() {
        Author second = Author.builder()
                .id(2L).name("J.K. Rowling").books(new ArrayList<>()).build();

        when(authorRepository.findAll()).thenReturn(List.of(author, second));

        List<AuthorResponse> responses = authorService.getAllAuthors();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(AuthorResponse::getName)
                .containsExactly("George Orwell", "J.K. Rowling");
    }

    // ── updateAuthor ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateAuthor — success")
    void updateAuthor_success() {
        AuthorRequest updateRequest = new AuthorRequest();
        updateRequest.setName("Eric Blair");
        updateRequest.setEmail("blair@books.com");

        Author updated = Author.builder()
                .id(1L).name("Eric Blair").email("blair@books.com").books(new ArrayList<>()).build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.findByEmail("blair@books.com")).thenReturn(Optional.empty());
        when(authorRepository.save(any())).thenReturn(updated);

        AuthorResponse response = authorService.updateAuthor(1L, updateRequest);

        assertThat(response.getName()).isEqualTo("Eric Blair");
        assertThat(response.getEmail()).isEqualTo("blair@books.com");
    }

    @Test
    @DisplayName("updateAuthor — throws DuplicateResourceException when email belongs to another author")
    void updateAuthor_emailTakenByOther_throws() {
        Author other = Author.builder().id(2L).name("Other").email("orwell@books.com").books(new ArrayList<>()).build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> authorService.updateAuthor(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}