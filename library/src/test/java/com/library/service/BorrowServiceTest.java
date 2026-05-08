package com.library.service;

import com.org.library.config.LibraryProperties;
import com.org.library.dto.request.BorrowRequest;
import com.org.library.dto.response.BorrowRecordResponse;
import com.org.library.entity.Author;
import com.org.library.entity.Book;
import com.org.library.entity.BorrowRecord;
import com.org.library.entity.Member;
import com.org.library.entity.enums.BorrowStatus;
import com.org.library.exception.BadRequestException;
import com.org.library.exception.BookNotAvailableException;
import com.org.library.exception.MaxBorrowLimitException;
import com.org.library.repository.BorrowRecordRepository;
import com.org.library.service.BookService;
import com.org.library.service.BorrowService;
import com.org.library.service.MemberService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowService tests")
class BorrowServiceTest {

    @Mock private BorrowRecordRepository borrowRecordRepository;
    @Mock private BookService            bookService;
    @Mock private MemberService          memberService;
    @Mock private LibraryProperties      libraryProperties;

    @InjectMocks
    private BorrowService borrowService;

    private Author       author;
    private Book         book;
    private Member       member;
    private BorrowRecord activeRecord;
    private BorrowRequest borrowRequest;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .id(1L).name("George Orwell").books(new ArrayList<>()).build();

        book = Book.builder()
                .id(1L).title("1984").isbn("978-0451524935")
                .available(true).author(author).borrowRecords(new ArrayList<>()).build();

        member = Member.builder()
                .id(1L).name("Alice").email("alice@lib.com")
                .borrowRecords(new ArrayList<>()).build();

        activeRecord = BorrowRecord.builder()
                .id(1L)
                .book(book)
                .member(member)
                .borrowedAt(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(BorrowStatus.ACTIVE)
                .build();

        borrowRequest = new BorrowRequest();
        borrowRequest.setBookId(1L);
        borrowRequest.setMemberId(1L);
    }

    // ── borrowBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("borrowBook — success: creates record, sets dates, flips availability")
    void borrowBook_success() {
        when(bookService.findBookOrThrow(1L)).thenReturn(book);
        when(memberService.findMemberOrThrow(1L)).thenReturn(member);
        when(borrowRecordRepository.countByMemberIdAndStatus(1L, BorrowStatus.ACTIVE)).thenReturn(0L);
        when(libraryProperties.getMaxActiveBorrowsPerMember()).thenReturn(3);
        when(libraryProperties.getLoanPeriodDays()).thenReturn(14);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(activeRecord);

        BorrowRecordResponse response = borrowService.borrowBook(borrowRequest);

        assertThat(response.getBookId()).isEqualTo(1L);
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(BorrowStatus.ACTIVE);
        assertThat(response.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));

        // Book must be flipped to unavailable
        assertThat(book.getAvailable()).isFalse();

        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("borrowBook — throws BookNotAvailableException when book is already borrowed")
    void borrowBook_bookNotAvailable_throws() {
        book.setAvailable(false);

        when(bookService.findBookOrThrow(1L)).thenReturn(book);
        when(memberService.findMemberOrThrow(1L)).thenReturn(member);

        assertThatThrownBy(() -> borrowService.borrowBook(borrowRequest))
                .isInstanceOf(BookNotAvailableException.class)
                .hasMessageContaining("1");

        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("borrowBook — throws MaxBorrowLimitException when member is at limit")
    void borrowBook_memberAtLimit_throws() {
        when(bookService.findBookOrThrow(1L)).thenReturn(book);
        when(memberService.findMemberOrThrow(1L)).thenReturn(member);
        when(borrowRecordRepository.countByMemberIdAndStatus(1L, BorrowStatus.ACTIVE)).thenReturn(3L);
        when(libraryProperties.getMaxActiveBorrowsPerMember()).thenReturn(3);

        assertThatThrownBy(() -> borrowService.borrowBook(borrowRequest))
                .isInstanceOf(MaxBorrowLimitException.class)
                .hasMessageContaining("3");

        verify(borrowRecordRepository, never()).save(any());
    }

    // ── returnBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("returnBook — success: sets returnedAt, status RETURNED, flips book available")
    void returnBook_success() {
        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(activeRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(activeRecord);

        BorrowRecordResponse response = borrowService.returnBook(1L);

        assertThat(response.getStatus()).isEqualTo(BorrowStatus.RETURNED);

        // Book must be flipped back to available
        assertThat(book.getAvailable()).isTrue();

        // returnedAt must be set to today
        assertThat(activeRecord.getReturnedAt()).isEqualTo(LocalDate.now());

        verify(borrowRecordRepository).save(activeRecord);
    }

    @Test
    @DisplayName("returnBook — throws BadRequestException when record is already RETURNED")
    void returnBook_alreadyReturned_throws() {
        activeRecord.setStatus(BorrowStatus.RETURNED);
        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(activeRecord));

        assertThatThrownBy(() -> borrowService.returnBook(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RETURNED");

        verify(borrowRecordRepository, never()).save(any());
    }

    // ── markOverdueRecords ────────────────────────────────────────────────────

    @Test
    @DisplayName("markOverdueRecords — flips ACTIVE past-due records to OVERDUE")
    void markOverdueRecords_flipsStatus() {
        BorrowRecord overdueRecord = BorrowRecord.builder()
                .id(2L).book(book).member(member)
                .borrowedAt(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(6))
                .status(BorrowStatus.ACTIVE)
                .build();

        when(borrowRecordRepository.findOverdueRecords(any(LocalDate.class)))
                .thenReturn(List.of(overdueRecord));
        when(borrowRecordRepository.saveAll(anyList())).thenReturn(List.of(overdueRecord));

        int count = borrowService.markOverdueRecords();

        assertThat(count).isEqualTo(1);
        assertThat(overdueRecord.getStatus()).isEqualTo(BorrowStatus.OVERDUE);
    }

    @Test
    @DisplayName("markOverdueRecords — returns 0 when no overdue records exist")
    void markOverdueRecords_noneOverdue() {
        when(borrowRecordRepository.findOverdueRecords(any(LocalDate.class)))
                .thenReturn(List.of());

        int count = borrowService.markOverdueRecords();

        assertThat(count).isZero();
        verify(borrowRecordRepository).saveAll(List.of());
    }

    // ── getBorrowsByMember ────────────────────────────────────────────────────

    @Test
    @DisplayName("getBorrowsByMember — returns all borrow records for a member")
    void getBorrowsByMember_returnsList() {
        when(memberService.findMemberOrThrow(1L)).thenReturn(member);
        when(borrowRecordRepository.findByMemberId(1L)).thenReturn(List.of(activeRecord));

        List<BorrowRecordResponse> responses = borrowService.getBorrowsByMember(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getMemberName()).isEqualTo("Alice");
        assertThat(responses.get(0).getBookTitle()).isEqualTo("1984");
    }
}