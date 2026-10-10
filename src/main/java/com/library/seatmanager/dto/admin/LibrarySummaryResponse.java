package com.library.seatmanager.dto.admin;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibrarySummaryResponse {

    private Long id;

    private String libraryName;

    private int totalSeats;

    private String logoUrl;
}