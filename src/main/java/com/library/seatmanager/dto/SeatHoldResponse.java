package com.library.seatmanager.dto;

import com.library.seatmanager.entity.SeatHold;

import java.time.LocalDateTime;

public class SeatHoldResponse {

    private Long id;
    private int seatNumber;
    private String name;
    private String phone;
    private LocalDateTime holdUntil;
    private LocalDateTime createdAt;
    private boolean active;

    public SeatHoldResponse() {
    }

    public static SeatHoldResponse from(SeatHold hold) {

        SeatHoldResponse response = new SeatHoldResponse();

        response.setId(hold.getId());

        response.setSeatNumber(
                hold.getSeat() != null
                        ? hold.getSeat().getSeatNumber()
                        : 0
        );

        response.setName(hold.getName());
        response.setPhone(hold.getPhone());
        response.setHoldUntil(hold.getHoldUntil());
        response.setCreatedAt(hold.getCreatedAt());
        response.setActive(hold.isActive());

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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