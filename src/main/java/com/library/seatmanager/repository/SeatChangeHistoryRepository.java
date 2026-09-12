package com.library.seatmanager.repository;

import com.library.seatmanager.entity.SeatChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatChangeHistoryRepository
        extends JpaRepository<SeatChangeHistory, Long> {

    List<SeatChangeHistory> findByStudentIdOrderByChangedAtDesc(
            Long studentId
    );
}
