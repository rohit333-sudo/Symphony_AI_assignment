package com.org.library.exception;


public class MaxBorrowLimitException extends RuntimeException {

    public MaxBorrowLimitException(int limit) {
        super("Member has reached the maximum borrow limit of " + limit + " active books");
    }
}
