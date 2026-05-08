package com.org.library.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponse {

    private Long    id;
    private String  title;
    private String  isbn;
    private Boolean available;

    // Flattened author info — avoids circular serialisation
    private Long   authorId;
    private String authorName;
}
