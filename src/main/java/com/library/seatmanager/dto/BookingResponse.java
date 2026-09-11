package com.library.seatmanager.dto;

import com.library.seatmanager.entity.Student;

public class BookingResponse {

    private String message;
    private StudentResponseDTO student;

    public BookingResponse(String message, StudentResponseDTO student) {
        this.message = message;
        this.student = student;
    }

    public BookingResponse(String message) {
        this.message = message;
        this.student = null;
    }

    public String getMessage() {
        return message;
    }

    public StudentResponseDTO getStudent() {
        return student;
    }
}