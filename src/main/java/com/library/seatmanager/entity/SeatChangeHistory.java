package com.library.seatmanager.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_change_history")
public class SeatChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student whose seat was changed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Library in which the change happened
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    // Previous seat
    @Column(name = "old_seat_number", nullable = false)
    private Integer oldSeatNumber;

    // New seat
    @Column(name = "new_seat_number", nullable = false)
    private Integer newSeatNumber;

    // When the change happened
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;


    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    public Integer getOldSeatNumber() {
        return oldSeatNumber;
    }

    public void setOldSeatNumber(Integer oldSeatNumber) {
        this.oldSeatNumber = oldSeatNumber;
    }

    public Integer getNewSeatNumber() {
        return newSeatNumber;
    }

    public void setNewSeatNumber(Integer newSeatNumber) {
        this.newSeatNumber = newSeatNumber;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}