package com.library.seatmanager.dto;

import java.time.LocalDateTime;

public class SeatStatusDTO {

    private int seatNumber;

    private boolean occupied;

    // ============================================================
    // SEAT HOLD
    // ============================================================

    private boolean held;

    private Long holdId;

    private String holdName;

    private String holdPhone;

    private LocalDateTime holdUntil;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public SeatStatusDTO() {
    }

    /*
     * Existing constructor.
     *
     * Keeps compatibility with existing code.
     */
    public SeatStatusDTO(
            int seatNumber,
            boolean occupied
    ) {
        this.seatNumber = seatNumber;
        this.occupied = occupied;
        this.held = false;
    }

    /*
     * New constructor.
     */
    public SeatStatusDTO(
            int seatNumber,
            boolean occupied,
            boolean held,
            Long holdId,
            String holdName,
            String holdPhone,
            LocalDateTime holdUntil
    ) {
        this.seatNumber = seatNumber;
        this.occupied = occupied;
        this.held = held;
        this.holdId = holdId;
        this.holdName = holdName;
        this.holdPhone = holdPhone;
        this.holdUntil = holdUntil;
    }

    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
    public Long getHoldId() {
        return holdId;
    }

    public void setHoldId(Long holdId) {
        this.holdId = holdId;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean isHeld() {
        return held;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public String getHoldName() {
        return holdName;
    }

    public void setHoldName(String holdName) {
        this.holdName = holdName;
    }

    public String getHoldPhone() {
        return holdPhone;
    }

    public void setHoldPhone(String holdPhone) {
        this.holdPhone = holdPhone;
    }

    public LocalDateTime getHoldUntil() {
        return holdUntil;
    }

    public void setHoldUntil(LocalDateTime holdUntil) {
        this.holdUntil = holdUntil;
    }

    // ============================================================
    // TO STRING
    // ============================================================

    @Override
    public String toString() {
        return "SeatStatusDTO{" +
                "seatNumber=" + seatNumber +
                ", occupied=" + occupied +
                ", held=" + held +
                ", holdId=" + holdId +
                ", holdName='" + holdName + '\'' +
                ", holdPhone='" + holdPhone + '\'' +
                ", holdUntil=" + holdUntil +
                '}';
    }
}