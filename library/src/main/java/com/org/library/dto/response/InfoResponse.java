package com.org.library.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InfoResponse {

    private String appName;
    private String appVersion;
    private int    loanPeriodDays;
    private int    maxActiveBorrowsPerMember;
}
