package com.library.seatmanager.dto;

import com.library.seatmanager.entity.SeatHold;

import java.time.LocalDateTime;

public class SeatHoldAlertResponse {

    private Long id;

    private String alertType;

    private String name;

    private String phone;

    private int seatNumber;

    private LocalDateTime holdUntil;

    private LocalDateTime createdAt;

    private boolean active;

    public SeatHoldAlertResponse() {
    }

    public SeatHoldAlertResponse(
            Long id,
            String alertType,
            String name,
            String phone,
            int seatNumber,
            LocalDateTime holdUntil,
            LocalDateTime createdAt,
            boolean active
    ) {
        this.id = id;
        this.alertType = alertType;
        this.name = name;
        this.phone = phone;
        this.seatNumber = seatNumber;
        this.holdUntil = holdUntil;
        this.createdAt = createdAt;
        this.active = active;
    }

    public static SeatHoldAlertResponse from(
            SeatHold seatHold,
            String alertType
    ) {
        return new SeatHoldAlertResponse(
                seatHold.getId(),
                alertType,
                seatHold.getName(),
                seatHold.getPhone(),
                seatHold.getSeat().getSeatNumber(),
                seatHold.getHoldUntil(),
                seatHold.getCreatedAt(),
                seatHold.isActive()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
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

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDateTime getHoldUntil() {
        return holdUntil;
    }

    public void setHoldUntil(LocalDateTime holdUntil) {
        this.holdUntil = holdUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}