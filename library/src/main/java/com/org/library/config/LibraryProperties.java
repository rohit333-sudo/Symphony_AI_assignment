package com.org.library.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "library")
@Getter
@Setter
public class LibraryProperties {

    /**
     * Number of days a member can keep a book before it is overdue.
     * Injected from: library.loan-period-days (default 14)
     */
    private int loanPeriodDays = 14;

    /**
     * Maximum number of books a member can have borrowed at the same time.
     * Injected from: library.max-active-borrows-per-member (default 3)
     */
    private int maxActiveBorrowsPerMember = 3;
}
