package com.library.seatmanager.dto;

import com.library.seatmanager.entity.Student;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HalfDayStudentUpdateRequest {
    private String name;

    private String phone;

    private int amount;

    private Student.HalfDaySlot halfDaySlot;

    private LocalDate expiryDate;

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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Student.HalfDaySlot getHalfDaySlot() {
        return halfDaySlot;
    }

    public void setHalfDaySlot(Student.HalfDaySlot halfDaySlot) {
        this.halfDaySlot = halfDaySlot;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
