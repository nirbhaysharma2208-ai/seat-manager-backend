package com.library.seatmanager.repository;

import com.library.seatmanager.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    // ============================================================
    // FIND ACTIVE HOLD FOR A PARTICULAR SEAT
    // ============================================================

    Optional<SeatHold> findByLibraryIdAndSeat_SeatNumberAndActiveTrue(
            Long libraryId,
            int seatNumber
    );

    // ============================================================
    // FIND EXPIRED HOLDS
    // ============================================================

    List<SeatHold>
    findByLibraryIdAndActiveTrueAndHoldUntilLessThanEqualOrderByHoldUntilDesc(
            Long libraryId,
            LocalDateTime now
    );

    // ============================================================
    // FIND ACTIVE / NON-EXPIRED HOLDS
    // ============================================================

    List<SeatHold>
    findByLibraryIdAndActiveTrueAndHoldUntilAfter(
            Long libraryId,
            LocalDateTime now
    );
}