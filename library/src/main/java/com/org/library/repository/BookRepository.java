package com.org.library.repository;

import com.org.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Duplicate ISBN guard on create / update
    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    // Supports GET /api/books?available=true
    List<Book> findByAvailableTrue();

    // All books written by a given author
    List<Book> findByAuthorId(Long authorId);

    // Case-insensitive title search (optional utility)
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Book> searchByTitle(@Param("title") String title);
}
