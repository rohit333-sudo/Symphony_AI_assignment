package com.org.library.service;

import com.org.library.dto.request.AuthorRequest;
import com.org.library.dto.response.AuthorResponse;
import com.org.library.entity.Author;
import com.org.library.exception.DuplicateResourceException;
import com.org.library.exception.ResourceNotFoundException;
import com.org.library.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public AuthorResponse createAuthor(AuthorRequest request) {
        // Guard: email must be unique if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && authorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Author with email '" + request.getEmail() + "' already exists");
        }

        Author author = Author.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        return toResponse(authorRepository.save(author));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<AuthorResponse> getAllAuthors() {
        List<AuthorResponse> responses = new ArrayList<>();
        for (Author author : authorRepository.findAll()) {
            responses.add(toResponse(author));
        }
        return responses;
    }
    public AuthorResponse getAuthorById(Long id) {
        return toResponse(findAuthorOrThrow(id));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public AuthorResponse updateAuthor(Long id, AuthorRequest request) {
        Author author = findAuthorOrThrow(id);

        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.isBlank()) {
            boolean emailTakenByOther = authorRepository.findByEmail(newEmail)
                    .map(existing -> !existing.getId().equals(id))
                    .orElse(false);

            if (emailTakenByOther) {
                throw new DuplicateResourceException("Email '" + newEmail + "' is already in use");
            }
        }

        author.setName(request.getName());
        author.setEmail(request.getEmail());

        return toResponse(authorRepository.save(author));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Author findAuthorOrThrow(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
    }

    private AuthorResponse toResponse(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .email(author.getEmail())
                .totalBooks(author.getBooks().size())
                .build();
    }
}
