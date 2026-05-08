package com.org.library.dto.response;

import com.org.library.entity.enums.BorrowStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BorrowRecordResponse {

    private Long id;

    // Book info — flattened
    private Long   bookId;
    private String bookTitle;
    private String bookIsbn;

    // Member info — flattened
    private Long   memberId;
    private String memberName;

    // Dates
    private LocalDate borrowedAt;
    private LocalDate dueDate;
    private LocalDate returnedAt;  // null until the book is returned

    private BorrowStatus status;

    // Computed convenience field: how many days past dueDate (0 if not overdue)
    private long daysOverdue;
}
