package com.org.library.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberResponse {

    private Long   id;
    private String name;
    private String email;
    private String phone;
    private int    activeBorrows;  // Current count of ACTIVE borrow records
}
