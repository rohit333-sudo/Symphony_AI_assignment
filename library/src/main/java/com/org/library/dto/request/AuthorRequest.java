package com.org.library.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorRequest {

    @NotBlank(message = "Author name is required")
    private String name;

    @Email(message = "Email must be a valid email address")
    private String email;          // Optional — no @NotBlank
}
