package com.org.library.exception;

public class BookNotAvailableException extends RuntimeException {

    public BookNotAvailableException(Long bookId) {
        super("Book with id " + bookId + " is already borrowed and not available");
    }
}
