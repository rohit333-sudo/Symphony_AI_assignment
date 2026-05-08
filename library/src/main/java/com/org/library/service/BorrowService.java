package com.org.library.service;

import com.org.library.config.LibraryProperties;
import com.org.library.dto.request.BorrowRequest;
import com.org.library.dto.response.BorrowRecordResponse;
import com.org.library.entity.Book;
import com.org.library.entity.BorrowRecord;
import com.org.library.entity.Member;
import com.org.library.entity.enums.BorrowStatus;
import com.org.library.exception.BadRequestException;
import com.org.library.exception.BookNotAvailableException;
import com.org.library.exception.MaxBorrowLimitException;
import com.org.library.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookService            bookService;
    private final MemberService          memberService;
    private final LibraryProperties      libraryProperties;

    // ── Borrow a book ─────────────────────────────────────────────────────────

    @Transactional
    public BorrowRecordResponse borrowBook(BorrowRequest request) {

        Book   book   = bookService.findBookOrThrow(request.getBookId());
        Member member = memberService.findMemberOrThrow(request.getMemberId());

        // Rule 1: Book must be available
        if (!book.getAvailable()) {
            throw new BookNotAvailableException(book.getId());
        }

        // Rule 2: Member must not exceed max active borrows
        long activeBorrows = borrowRecordRepository
                .countByMemberIdAndStatus(member.getId(), BorrowStatus.ACTIVE);

        if (activeBorrows >= libraryProperties.getMaxActiveBorrowsPerMember()) {
            throw new MaxBorrowLimitException(libraryProperties.getMaxActiveBorrowsPerMember());
        }

        // All checks passed — create the borrow record
        LocalDate today   = LocalDate.now();
        LocalDate dueDate = today.plusDays(libraryProperties.getLoanPeriodDays());

        BorrowRecord record = BorrowRecord.builder()
                .book(book)
                .member(member)
                .borrowedAt(today)
                .dueDate(dueDate)
                .returnedAt(null)
                .status(BorrowStatus.ACTIVE)
                .build();

        // Mark book as unavailable
        book.setAvailable(false);

        return toResponse(borrowRecordRepository.save(record));
    }

    // ── Return a book ─────────────────────────────────────────────────────────

    @Transactional
    public BorrowRecordResponse returnBook(Long borrowRecordId) {
        BorrowRecord record = findRecordOrThrow(borrowRecordId);

        // Only RETURNED should be blocked
        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BadRequestException(
                    "Borrow record " + borrowRecordId + " is already returned");
        }

        LocalDate today = LocalDate.now();

        record.setReturnedAt(today);
        record.setStatus(BorrowStatus.RETURNED);

        // Mark book as available again
        record.getBook().setAvailable(true);

        return toResponse(borrowRecordRepository.save(record));
    }

    // ── Borrow history for a member ───────────────────────────────────────────

    public List<BorrowRecordResponse> getBorrowsByMember(Long memberId) {
        // Verify member exists first
        memberService.findMemberOrThrow(memberId);

        return borrowRecordRepository.findByMemberId(memberId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Mark overdue records ──────────────────────────────────────────────────
    // Called by a scheduled task (or manually) to flip ACTIVE → OVERDUE
    // for any record whose dueDate has passed.


    // Step 2 — service
    @Scheduled(cron = "0 0 0 * * *")  // every midnight
    public void scheduledOverdueCheck() {
        markOverdueRecords();
        int count = markOverdueRecords();
        System.out.println("Scheduled overdue check ran — " + count + " records marked overdue");

    }
    @Transactional
    public int markOverdueRecords() {
        List<BorrowRecord> overdue =
                borrowRecordRepository.findOverdueRecords(LocalDate.now());

        overdue.forEach(r -> r.setStatus(BorrowStatus.OVERDUE));
        borrowRecordRepository.saveAll(overdue);

        return overdue.size();
    }


    public List<BorrowRecordResponse> getAllBorrows() {

        // Step 1 — fetch all records
        List<BorrowRecord> records = borrowRecordRepository.findAll();

        // Step 2 — convert to response
        List<BorrowRecordResponse> responses = new ArrayList<>();
        for (BorrowRecord record : records) {
            responses.add(toResponse(record));
        }
        return responses;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BorrowRecord findRecordOrThrow(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new com.org.library.exception.ResourceNotFoundException(
                        "BorrowRecord", id));
    }

    private BorrowRecordResponse toResponse(BorrowRecord record) {
        LocalDate today    = LocalDate.now();
        LocalDate dueDate  = record.getDueDate();

        // daysOverdue is positive only if still ACTIVE/OVERDUE and past dueDate
        long daysOverdue = 0;
        if (record.getStatus() != BorrowStatus.RETURNED && today.isAfter(dueDate)) {
            daysOverdue = dueDate.until(today).getDays();
        }

        return BorrowRecordResponse.builder()
                .id(record.getId())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .bookIsbn(record.getBook().getIsbn())
                .memberId(record.getMember().getId())
                .memberName(record.getMember().getName())
                .borrowedAt(record.getBorrowedAt())
                .dueDate(record.getDueDate())
                .returnedAt(record.getReturnedAt())
                .status(record.getStatus())
                .daysOverdue(daysOverdue)
                .build();
    }
}
