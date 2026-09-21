package com.library.seatmanager.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seat_holds",
        indexes = {
                @Index(name = "idx_seat_hold_library_seat", columnList = "library_id, seat_id"),
                @Index(name = "idx_seat_hold_until", columnList = "hold_until")
        }
)
@Data
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // SEAT
    // ============================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    // ============================================================
    // LIBRARY
    // ============================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    // ============================================================
    // PROSPECT DETAILS
    // ============================================================

    @Column(nullable = false)
    private String name;

    private String phone;

    // ============================================================
    // HOLD INFORMATION
    // ============================================================

    @Column(name = "hold_until", nullable = false)
    private LocalDateTime holdUntil;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public SeatHold() {
    }

    public SeatHold(
            Seat seat,
            Library library,
            String name,
            String phone,
            LocalDateTime holdUntil
    ) {
        this.seat = seat;
        this.library = library;
        this.name = name;
        this.phone = phone;
        this.holdUntil = holdUntil;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}