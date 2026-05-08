package com.org.library.repository;

import com.org.library.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Used to check duplicate email on create / update
    Optional<Author> findByEmail(String email);

    boolean existsByEmail(String email);
}
