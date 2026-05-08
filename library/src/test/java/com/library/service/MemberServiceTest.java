package com.org.library.service;

import com.org.library.dto.request.MemberRequest;
import com.org.library.dto.response.MemberResponse;
import com.org.library.entity.Member;
import com.org.library.entity.enums.BorrowStatus;
import com.org.library.exception.DuplicateResourceException;
import com.org.library.repository.BorrowRecordRepository;
import com.org.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void registerMember_success() {
        MemberRequest req = new MemberRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPhone("123");

        when(memberRepository.existsByEmail("john@example.com")).thenReturn(false);

        Member saved = Member.builder().id(2L).name("John Doe").email("john@example.com").phone("123").build();
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        when(borrowRecordRepository.countByMemberIdAndStatus(2L, BorrowStatus.ACTIVE)).thenReturn(0L);

        MemberResponse resp = memberService.registerMember(req);

        assertEquals(2L, resp.getId());
        assertEquals("John Doe", resp.getName());
        assertEquals("john@example.com", resp.getEmail());
    }

    @Test
    void registerMember_duplicateEmail_throws() {
        MemberRequest req = new MemberRequest();
        req.setName("Jane");
        req.setEmail("jane@example.com");

        when(memberRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> memberService.registerMember(req));
    }
}
