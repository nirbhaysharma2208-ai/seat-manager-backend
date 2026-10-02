package com.library.seatmanager.dto;

public class UpdateLibraryProfileRequest {

    private String libraryName;
    private Integer totalSeats;

    public UpdateLibraryProfileRequest() {
    }

    public UpdateLibraryProfileRequest(String libraryName, Integer totalSeats) {
        this.libraryName = libraryName;
        this.totalSeats = totalSeats;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
}