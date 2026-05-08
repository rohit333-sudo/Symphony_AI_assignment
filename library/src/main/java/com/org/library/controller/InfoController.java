package com.org.library.controller;

import com.org.library.config.AppProperties;
import com.org.library.config.LibraryProperties;
import com.org.library.dto.response.InfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info")
@RequiredArgsConstructor
public class InfoController {

    private final AppProperties     appProperties;
    private final LibraryProperties libraryProperties;

    // GET /api/info
    @GetMapping
    public ResponseEntity<InfoResponse> getInfo() {
        return ResponseEntity.ok(InfoResponse.builder()
                .appName(appProperties.getName())
                .appVersion(appProperties.getVersion())
                .loanPeriodDays(libraryProperties.getLoanPeriodDays())
                .maxActiveBorrowsPerMember(libraryProperties.getMaxActiveBorrowsPerMember())
                .build());
    }
}
