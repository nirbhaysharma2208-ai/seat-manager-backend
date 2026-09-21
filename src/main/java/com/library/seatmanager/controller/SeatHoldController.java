package com.library.seatmanager.controller;

import com.library.seatmanager.dto.SeatHoldAlertResponse;
import com.library.seatmanager.dto.SeatHoldRequest;
import com.library.seatmanager.dto.SeatHoldResponse;
import com.library.seatmanager.entity.Library;
import com.library.seatmanager.entity.Seat;
import com.library.seatmanager.entity.SeatHold;
import com.library.seatmanager.repository.LibraryRepository;
import com.library.seatmanager.repository.SeatHoldRepository;
import com.library.seatmanager.repository.SeatRepository;
import com.library.seatmanager.repository.StudentRepository;
import com.library.seatmanager.service.SecurityService;


import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/seat-holds")
@CrossOrigin
public class SeatHoldController {

    @Autowired
    private SeatHoldRepository seatHoldRepo;

    @Autowired
    private SeatRepository seatRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private LibraryRepository libraryRepo;

    @Autowired
    private SecurityService securityService;

    // ============================================================
    // CREATE HOLD
    // ============================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Transactional
    public ResponseEntity<?> createHold(
            Authentication auth,
            @RequestBody SeatHoldRequest request
    ) {

        if (request == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Hold request is required"
                            )
                    );
        }

        if (request.getLibraryId() == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Library ID is required"
                            )
                    );
        }

        if (request.getName() == null ||
                request.getName().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Name is required"
                            )
                    );
        }

        /*
         * User requirement:
         *
         * Hold only for 1 or 2 days.
         */
        if (request.getDays() != 1 &&
                request.getDays() != 2) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Hold duration must be 1 or 2 days"
                            )
                    );
        }

        // ========================================================
        // SECURITY
        // ========================================================

        securityService.validateLibraryAccess(
                request.getLibraryId(),
                auth
        );

        // ========================================================
        // LIBRARY
        // ========================================================

        Library library =
                libraryRepo.findById(
                        request.getLibraryId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Library not found"
                        )
                );

        // ========================================================
        // LOCK SEAT
        // ========================================================

        Seat seat =
                seatRepo
                        .findByLibraryIdAndSeatNumberForUpdate(
                                request.getLibraryId(),
                                request.getSeatNumber()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Seat not found for this library"
                                )
                        );

        // ========================================================
        // OCCUPIED CHECK
        // ========================================================

        Optional<?> activeStudent =
                studentRepo
                        .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                                request.getLibraryId(),
                                request.getSeatNumber()
                        );

        if (activeStudent.isPresent()) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            Map.of(
                                    "message",
                                    "Seat is already occupied"
                            )
                    );
        }

        // ========================================================
        // EXISTING HOLD
        // ========================================================

        Optional<SeatHold> existingHold =
                seatHoldRepo
                        .findByLibraryIdAndSeat_SeatNumberAndActiveTrue(
                                request.getLibraryId(),
                                request.getSeatNumber()
                        );

        if (existingHold.isPresent()) {

            SeatHold oldHold =
                    existingHold.get();

            if (oldHold.getHoldUntil()
                    .isAfter(LocalDateTime.now())) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(
                                Map.of(
                                        "message",
                                        "Seat is already held for " +
                                                oldHold.getName(),
                                        "holdUntil",
                                        oldHold.getHoldUntil()
                                )
                        );
            }

            /*
             * Previous hold has expired.
             *
             * Mark it inactive before creating the new hold.
             */
            oldHold.setActive(false);

            seatHoldRepo.save(oldHold);
        }

        // ========================================================
        // CREATE HOLD
        // ========================================================

        LocalDateTime holdUntil =
                LocalDateTime.now()
                        .plusDays(request.getDays());

        SeatHold hold =
                new SeatHold();

        hold.setSeat(seat);

        hold.setLibrary(library);

        hold.setName(
                request.getName().trim()
        );

        hold.setPhone(
                request.getPhone()
        );

        hold.setHoldUntil(
                holdUntil
        );

        hold.setActive(true);

        hold.setCreatedAt(
                LocalDateTime.now()
        );

        seatHoldRepo.save(hold);

        // ========================================================
        // RESPONSE
        // ========================================================

        return ResponseEntity.ok(
                SeatHoldResponse.from(hold)
        );
    }

    // ============================================================
    // GET CURRENT HOLD FOR SEAT
    // ============================================================

    @GetMapping("/library/{libraryId}/seat/{seatNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'ACCOUNTANT')")
    public ResponseEntity<?> getSeatHold(
            Authentication auth,
            @PathVariable Long libraryId,
            @PathVariable int seatNumber
    ) {

        securityService.validateLibraryAccess(
                libraryId,
                auth
        );

        Optional<SeatHold> optionalHold =
                seatHoldRepo
                        .findByLibraryIdAndSeat_SeatNumberAndActiveTrue(
                                libraryId,
                                seatNumber
                        );

        if (optionalHold.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SeatHold hold =
                optionalHold.get();

        /*
         * Expired hold is no longer considered active
         * from the application's perspective.
         */
        if (!hold.getHoldUntil()
                .isAfter(LocalDateTime.now())) {

            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                SeatHoldResponse.from(hold)
        );
    }

    // ============================================================
    // CANCEL HOLD
    // ============================================================

    @DeleteMapping("/{holdId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Transactional
    public ResponseEntity<?> cancelHold(
            Authentication auth,
            @PathVariable Long holdId
    ) {

        SeatHold hold =
                seatHoldRepo.findById(holdId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Seat hold not found"
                                )
                        );

        if (hold.getLibrary() == null) {

            throw new RuntimeException(
                    "Seat hold has no library"
            );
        }

        securityService.validateLibraryAccess(
                hold.getLibrary().getId(),
                auth
        );

        hold.setActive(false);

        seatHoldRepo.save(hold);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Seat hold cancelled successfully"
                )
        );
    }

    // ============================================================
    // EXPIRED SEAT HOLD ALERTS
    // ============================================================

    // ============================================================
// EXPIRED SEAT HOLD ALERTS
// ============================================================

    @GetMapping("/alerts/{libraryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'ACCOUNTANT')")
    public List<SeatHoldAlertResponse> getExpiredHoldAlerts(
            Authentication auth,
            @PathVariable Long libraryId
    ) {

        securityService.validateLibraryAccess(
                libraryId,
                auth
        );

        LocalDateTime now =
                LocalDateTime.now();

        List<SeatHold> expiredHolds =
                seatHoldRepo
                        .findByLibraryIdAndActiveTrueAndHoldUntilLessThanEqualOrderByHoldUntilDesc(
                                libraryId,
                                now
                        );

        return expiredHolds
                .stream()
                .map(hold ->
                        SeatHoldAlertResponse.from(
                                hold,
                                "SEAT_HOLD_EXPIRED"
                        )
                )
                .toList();
    }


// ============================================================
// ACTIVE SEAT HOLDS
// ============================================================

    @GetMapping("/library/{libraryId}/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<?> getActiveSeatHolds(
            Authentication auth,
            @PathVariable Long libraryId
    ) {

        try {

            securityService.validateLibraryAccess(
                    libraryId,
                    auth
            );

            LocalDateTime now =
                    LocalDateTime.now();

            List<SeatHold> activeHolds =
                    seatHoldRepo
                            .findByLibraryIdAndActiveTrueAndHoldUntilAfter(
                                    libraryId,
                                    now
                            );

            List<SeatHoldAlertResponse> response =
                    activeHolds.stream()
                            .map(hold ->
                                    SeatHoldAlertResponse.from(
                                            hold,
                                            "SEAT_HOLD_ACTIVE"
                                    )
                            )
                            .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unable to load active seat holds"
                            )
                    );
        }
    }


}