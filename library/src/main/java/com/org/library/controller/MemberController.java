package com.org.library.controller;

import com.org.library.dto.request.MemberRequest;
import com.org.library.dto.response.BorrowRecordResponse;
import com.org.library.dto.response.MemberResponse;
import com.org.library.service.BorrowService;
import com.org.library.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final BorrowService borrowService;

    // POST /api/members
    @PostMapping
    public ResponseEntity<MemberResponse> registerMember(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(memberService.registerMember(request));
    }

    // GET /api/members
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    // GET /api/members/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    // PUT /api/members/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    // GET /api/members/{id}/borrows
    @GetMapping("/{id}/borrows")
    public ResponseEntity<List<BorrowRecordResponse>> getMemberBorrows(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.getBorrowsByMember(id));
    }
}
