package com.org.library.repository;

import com.org.library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Duplicate email guard on create / update
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
