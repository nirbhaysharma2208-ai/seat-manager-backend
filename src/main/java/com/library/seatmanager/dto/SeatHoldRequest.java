package com.library.seatmanager.dto;

public class SeatHoldRequest {

    private Long libraryId;
    private int seatNumber;
    private String name;
    private String phone;
    private int days;

    public SeatHoldRequest() {
    }

    public SeatHoldRequest(
            Long libraryId,
            int seatNumber,
            String name,
            String phone,
            int days
    ) {
        this.libraryId = libraryId;
        this.seatNumber = seatNumber;
        this.name = name;
        this.phone = phone;
        this.days = days;
    }

    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return "SeatHoldRequest{" +
                "libraryId=" + libraryId +
                ", seatNumber=" + seatNumber +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", days=" + days +
                '}';
    }
}