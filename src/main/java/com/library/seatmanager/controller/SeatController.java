package com.library.seatmanager.controller;

import com.library.seatmanager.dto.SeatStatusDTO;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Seat;
import com.library.seatmanager.entity.SeatHold;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.SeatHoldRepository;
import com.library.seatmanager.repository.SeatRepository;
import com.library.seatmanager.repository.StudentRepository;
import com.library.seatmanager.service.SecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin
public class SeatController {

    @Autowired
    private SeatRepository seatRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private SeatHoldRepository seatHoldRepo;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AdminRepository adminRepo;

    // ============================================================
    // GET ALL SEATS
    // ============================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'ACCOUNTANT')")
    public List<SeatStatusDTO> getAllSeats(
            Authentication auth
    ) {

        String phone = auth.getName();

        adminRepo.findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );

        List<Seat> seats =
                seatRepo.findAll(
                        Sort.by("seatNumber")
                );

        List<SeatStatusDTO> result =
                new ArrayList<>();

        LocalDateTime now =
                LocalDateTime.now();

        for (Seat seat : seats) {

            Long libraryId =
                    seat.getLibrary() != null
                            ? seat.getLibrary().getId()
                            : null;

            boolean occupied = false;

            if (libraryId != null) {

                occupied =
                        studentRepo
                                .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                                        libraryId,
                                        seat.getSeatNumber()
                                )
                                .isPresent();
            }

            SeatHold activeHold =
                    getActiveHold(
                            libraryId,
                            seat.getSeatNumber(),
                            now
                    );

            result.add(
                    buildSeatStatus(
                            seat,
                            occupied,
                            activeHold
                    )
            );
        }

        return result;
    }

    // ============================================================
    // GET SEATS BY LIBRARY
    // ============================================================

    @PreAuthorize(
            "hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'ACCOUNTANT')"
    )
    @GetMapping("/library/{libraryId}")
    public List<SeatStatusDTO> getSeatsByLibrary(
            @PathVariable Long libraryId,
            Authentication auth
    ) {

        securityService.validateLibraryAccess(
                libraryId,
                auth
        );

        List<Seat> seats =
                seatRepo.findByLibraryIdOrderBySeatNumberAsc(
                        libraryId
                );

        List<SeatStatusDTO> result =
                new ArrayList<>();

        LocalDateTime now =
                LocalDateTime.now();

        for (Seat seat : seats) {

            boolean occupied = false;

            try {

                occupied =
                        studentRepo
                                .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                                        libraryId,
                                        seat.getSeatNumber()
                                )
                                .isPresent();

            } catch (Exception e) {

                System.out.println(
                        "⚠️ Failed to check occupied seat " +
                                seat.getSeatNumber() +
                                ": " +
                                e.getMessage()
                );
            }

            SeatHold activeHold =
                    getActiveHold(
                            libraryId,
                            seat.getSeatNumber(),
                            now
                    );

            /*
             * IMPORTANT:
             *
             * An expired hold is NOT returned as held.
             *
             * Therefore the frontend automatically sees
             * the seat as available again.
             */
            result.add(
                    buildSeatStatus(
                            seat,
                            occupied,
                            activeHold
                    )
            );
        }

        return result;
    }

    // ============================================================
    // GET ACTIVE HOLD
    // ============================================================

    private SeatHold getActiveHold(
            Long libraryId,
            int seatNumber,
            LocalDateTime now
    ) {

        if (libraryId == null) {
            return null;
        }

        Optional<SeatHold> optionalHold =
                seatHoldRepo
                        .findByLibraryIdAndSeat_SeatNumberAndActiveTrue(
                                libraryId,
                                seatNumber
                        );

        if (optionalHold.isEmpty()) {
            return null;
        }

        SeatHold hold =
                optionalHold.get();

        /*
         * Hold exists in DB but has expired.
         *
         * We don't deactivate it here because the expired
         * record is still required by the Alerts section.
         */
        if (!hold.getHoldUntil().isAfter(now)) {
            return null;
        }

        return hold;
    }

    // ============================================================
    // BUILD DTO
    // ============================================================

    private SeatStatusDTO buildSeatStatus(
            Seat seat,
            boolean occupied,
            SeatHold hold
    ) {

        if (hold == null) {

            return new SeatStatusDTO(
                    seat.getSeatNumber(),
                    occupied,
                    false,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new SeatStatusDTO(
                seat.getSeatNumber(),
                occupied,
                true,
                hold.getId(),
                hold.getName(),
                hold.getPhone(),
                hold.getHoldUntil()
        );
    }
}