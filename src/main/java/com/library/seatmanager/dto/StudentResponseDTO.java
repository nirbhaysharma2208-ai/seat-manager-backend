package com.library.seatmanager.dto;

import com.library.seatmanager.entity.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentResponseDTO {

    private Long id;

    private String name;
    private String phone;

    private Integer seatNumber;

    private int amount;

    private LocalDate bookingDate;
    private LocalDate expiryDate;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private int amountPaid;

    private boolean active;

    private Student.StudentType studentType;

    private Student.HalfDaySlot halfDaySlot;

    // Constructor
    public StudentResponseDTO(
            Long id,
            String name,
            String phone,
            Integer seatNumber,
            int amount,
            LocalDate bookingDate,
            LocalDate expiryDate,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int amountPaid,
            boolean active,
            Student.StudentType studentType,
            Student.HalfDaySlot halfDaySlot
    ) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.seatNumber = seatNumber;
        this.amount = amount;
        this.bookingDate = bookingDate;
        this.expiryDate = expiryDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amountPaid = amountPaid;
        this.active = active;
        this.studentType = studentType;
        this.halfDaySlot = halfDaySlot;
    }

//    Getter Setter method


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public int getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(int amountPaid) {
        this.amountPaid = amountPaid;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Student.StudentType getStudentType() {
        return studentType;
    }

    public void setStudentType(Student.StudentType studentType) {
        this.studentType = studentType;
    }

    public Student.HalfDaySlot getHalfDaySlot() {
        return halfDaySlot;
    }

    public void setHalfDaySlot(Student.HalfDaySlot halfDaySlot) {
        this.halfDaySlot = halfDaySlot;
    }

    public static StudentResponseDTO fromStudent(Student student) {

        return new StudentResponseDTO(
                student.getId(),
                student.getName(),
                student.getPhone(),
                student.getSeatNumber(),
                student.getAmount(),
                student.getBookingDate(),
                student.getExpiryDate(),
                student.getStartDate(),
                student.getEndDate(),
                student.getAmountPaid(),
                student.isActive(),
                student.getStudentType(),
                student.getHalfDaySlot()
        );
    }
}

