package com.org.library.service;

import com.org.library.dto.request.MemberRequest;
import com.org.library.dto.response.MemberResponse;
import com.org.library.entity.Member;
import com.org.library.entity.enums.BorrowStatus;
import com.org.library.exception.DuplicateResourceException;
import com.org.library.exception.ResourceNotFoundException;
import com.org.library.repository.BorrowRecordRepository;
import com.org.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository       memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse registerMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Member with email '" + request.getEmail() + "' already exists");
        }

        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        return toResponse(memberRepository.save(member));
    }

    // ── Read ──────────────────────────────────────────────────────────────────
    public List<MemberResponse> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        List<MemberResponse> responses = new ArrayList<>();
        for (Member member : members) {
            responses.add(toResponse(member));
        }
        return responses;
    }

    public MemberResponse getMemberById(Long id) {
        return toResponse(findMemberOrThrow(id));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse updateMember(Long id, MemberRequest request) {
        Member member = findMemberOrThrow(id);

        boolean emailTakenByOther = memberRepository.findByEmail(request.getEmail())
                .map(existing -> !existing.getId().equals(id))
                .orElse(false);

        if (emailTakenByOther) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already in use");
        }

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());

        return toResponse(memberRepository.save(member));
    }
    // ── Helpers ───────────────────────────────────────────────────────────────

    public Member findMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    private MemberResponse toResponse(Member member) {
        long activeBorrows = borrowRecordRepository
                .countByMemberIdAndStatus(member.getId(), BorrowStatus.ACTIVE);

        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .activeBorrows((int) activeBorrows)
                .build();
    }
}
