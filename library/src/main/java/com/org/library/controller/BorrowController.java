package com.org.library.controller;

import com.org.library.dto.request.BorrowRequest;
import com.org.library.dto.response.BorrowRecordResponse;
import com.org.library.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    // POST /api/borrows
    @PostMapping
    public ResponseEntity<BorrowRecordResponse> borrowBook(
            @Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(borrowService.borrowBook(request));
    }

    // PUT /api/borrows/{id}/return
    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowRecordResponse> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.returnBook(id));
    }
    @GetMapping
    public ResponseEntity<List<BorrowRecordResponse>> getAll()
    {
       return ResponseEntity.ok( borrowService.getAllBorrows());

    }
}
