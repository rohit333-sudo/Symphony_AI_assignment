package com.org.library.repository;

import com.org.library.entity.BorrowRecord;
import com.org.library.entity.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    // ── Core borrow-prevention guard ──────────────────────────────────────────
    // Called before every borrow: if an ACTIVE record exists for this book, reject.
    Optional<BorrowRecord> findByBookIdAndStatus(Long bookId, BorrowStatus status);

    // ── History queries ───────────────────────────────────────────────────────

    // GET /api/members/{id}/borrows — full borrow history for a member
    List<BorrowRecord> findByMemberId(Long memberId);

    // Full borrow history for a specific book
    List<BorrowRecord> findByBookId(Long bookId);

    // Fetch all records by status (e.g. all ACTIVE, all OVERDUE)
    List<BorrowRecord> findByStatus(BorrowStatus status);

    // ── Business-rule enforcement ─────────────────────────────────────────────

    // Count of ACTIVE borrows for a member — enforces max-active-borrows-per-member
    long countByMemberIdAndStatus(Long memberId, BorrowStatus status);

    // All ACTIVE records whose dueDate has already passed — used by overdue scheduler
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'ACTIVE' AND br.dueDate < :today")
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today);

    // Spring Data JPA provides this automatically
    List<BorrowRecord> findAll();
}
