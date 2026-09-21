package com.library.seatmanager.dto;

import com.library.seatmanager.entity.Student;

public class StudentDetailResponse {

    private Long id;
    private String name;
    private String phone;
    private Integer seatNumber;

    private String bookingDate;
    private String expiryDate;

    private int amountPaid;

    private String studentType;
    private String halfDaySlot;

    public StudentDetailResponse() {
    }

    public StudentDetailResponse(Student student) {

        this.id = student.getId();

        this.name = student.getName();

        this.phone = student.getPhone();

        this.seatNumber = student.getSeatNumber();

        this.bookingDate =
                student.getBookingDate() != null
                        ? student.getBookingDate().toString()
                        : null;

        this.expiryDate =
                student.getExpiryDate() != null
                        ? student.getExpiryDate().toString()
                        : null;

        this.amountPaid = student.getAmountPaid();

        this.studentType =
                student.getStudentType() != null
                        ? student.getStudentType().name()
                        : null;

        this.halfDaySlot =
                student.getHalfDaySlot() != null
                        ? student.getHalfDaySlot().name()
                        : null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public int getAmountPaid() {
        return amountPaid;
    }

    public String getStudentType() {
        return studentType;
    }

    public String getHalfDaySlot() {
        return halfDaySlot;
    }
}