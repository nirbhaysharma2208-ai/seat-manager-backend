package com.library.seatmanager.controller;

import com.library.seatmanager.dto.*;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Library;
import com.library.seatmanager.entity.Seat;
import com.library.seatmanager.entity.SeatHold;
import com.library.seatmanager.entity.Student;
import com.library.seatmanager.repository.AdminRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/book")
@CrossOrigin
public class BookingController {

    @Autowired
    private SeatRepository seatRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private LibraryRepository libraryRepo;

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private SeatHoldRepository seatHoldRepo;

    @Autowired
    private SecurityService securityService;

    // ============================================================
    // SINGLE BOOKING
    // ============================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Transactional
    public ResponseEntity<BookingResponse> bookSeat(
            Authentication authentication,
            @RequestBody BookingRequest req
    ) {

        if (req == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new BookingResponse("Booking request is required"));
        }

        if (req.getLibraryId() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new BookingResponse("Library ID is required"));
        }

        if (req.getName() == null || req.getName().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(new BookingResponse("Student name is required"));
        }

        // ========================================================
        // AUTHENTICATION
        // ========================================================

        String phone = authentication.getName();

        Admin admin = adminRepo.findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );

        // ========================================================
        // LIBRARY
        // ========================================================

        Library library = libraryRepo.findById(req.getLibraryId())
                .orElseThrow(() ->
                        new RuntimeException("Library not found")
                );

        securityService.validateLibraryAccess(
                req.getLibraryId(),
                authentication
        );

        // ========================================================
        // LOCK SEAT
        // ========================================================

        Seat seat = seatRepo
                .findByLibraryIdAndSeatNumberForUpdate(
                        req.getLibraryId(),
                        req.getSeatNumber()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Seat not found for this library"
                        )
                );

        // ========================================================
        // CHECK ACTIVE STUDENT
        // ========================================================

        Optional<Student> activeStudent =
                studentRepo
                        .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                                req.getLibraryId(),
                                req.getSeatNumber()
                        );

        if (activeStudent.isPresent()) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            new BookingResponse(
                                    "Seat already occupied"
                            )
                    );
        }

        // ========================================================
        // CHECK SEAT HOLD
        // ========================================================

        Optional<SeatHold> existingHold =
                seatHoldRepo
                        .findByLibraryIdAndSeat_SeatNumberAndActiveTrue(
                                req.getLibraryId(),
                                req.getSeatNumber()
                        );

        if (existingHold.isPresent()) {

            SeatHold hold = existingHold.get();

            LocalDateTime now = LocalDateTime.now();

            // ----------------------------------------------------
            // EXPIRED HOLD
            // ----------------------------------------------------

            if (!hold.getHoldUntil().isAfter(now)) {

                /*
                 * Hold has expired.
                 *
                 * It no longer blocks booking.
                 */
                hold.setActive(false);
                seatHoldRepo.save(hold);

            } else {

                // ------------------------------------------------
                // ACTIVE HOLD
                // ------------------------------------------------

                if (req.getHoldId() == null ||
                        !req.getHoldId().equals(hold.getId())) {

                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(
                                    new BookingResponse(
                                            "Seat is currently held for " +
                                                    hold.getName()
                                    )
                            );
                }

                /*
                 * Correct holdId.
                 *
                 * This booking is converting the hold.
                 */
            }
        }

        // ========================================================
        // CREATE STUDENT
        // ========================================================

        Student student = new Student();

        student.setName(req.getName().trim());

        student.setPhone(req.getPhone());

        student.setSeat(seat);

        student.setSeatNumber(
                seat.getSeatNumber()
        );

        student.setAmountPaid(
                req.getAmountPaid()
        );

        student.setBookingDate(
                LocalDate.now()
        );

        student.setStartDate(
                LocalDateTime.now()
        );

        student.setExpiryDate(
                LocalDate.now().plusDays(30)
        );

        student.setEndDate(
                LocalDateTime.now().plusDays(30)
        );

        student.setStudentType(
                req.getStudentType() != null
                        ? req.getStudentType()
                        : Student.StudentType.FULL_DAY
        );

        student.setLibrary(library);

        student.setActive(true);

        // ========================================================
        // SAVE STUDENT
        // ========================================================

        studentRepo.save(student);

        // ========================================================
        // KEEP SEAT OCCUPANCY FLAG IN SYNC
        // ========================================================

        seat.setOccupied(true);

        seatRepo.save(seat);

        // ========================================================
        // REMOVE HOLD AFTER SUCCESSFUL BOOKING
        // ========================================================

        if (req.getHoldId() != null) {

            seatHoldRepo.findById(req.getHoldId())
                    .ifPresent(hold -> {

                        if (hold.getLibrary() != null &&
                                hold.getLibrary().getId()
                                        .equals(req.getLibraryId()) &&
                                hold.getSeat() != null &&
                                hold.getSeat().getSeatNumber()
                                        == req.getSeatNumber()) {

                            hold.setActive(false);

                            seatHoldRepo.save(hold);
                        }
                    });
        }

        // ========================================================
        // RESPONSE
        // ========================================================

        StudentResponseDTO response =
                StudentResponseDTO.fromStudent(student);

        return ResponseEntity.ok(
                new BookingResponse(
                        "Seat booked successfully",
                        response
                )
        );
    }

    // ============================================================
    // BULK BOOKING
    // ============================================================

    @Transactional
    @PostMapping("/bulk/library/{libraryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<?> bookMultipleSeats(
            Authentication authentication,
            @PathVariable Long libraryId,
            @RequestBody BulkBookingRequest request
    ) {

        securityService.validateLibraryAccess(
                libraryId,
                authentication
        );

        Library library = libraryRepo.findById(libraryId)
                .orElseThrow(() ->
                        new RuntimeException("Library not found")
                );

        List<String> bookedSeats = new ArrayList<>();

        List<String> failedSeats = new ArrayList<>();

        if (request == null ||
                request.getStudents() == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Students are required"
                            )
                    );
        }

        for (StudentCreateRequest dto :
                request.getStudents()) {

            try {

                // ==================================================
                // FIND + LOCK SEAT
                // ==================================================

                Seat seat = seatRepo
                        .findByLibraryIdAndSeatNumberForUpdate(
                                libraryId,
                                dto.getSeatNumber()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Seat not found"
                                )
                        );

                // ==================================================
                // OCCUPIED CHECK
                // ==================================================

                if (seat.isOccupied()) {

                    failedSeats.add(
                            "Seat " +
                                    dto.getSeatNumber() +
                                    " already occupied"
                    );

                    continue;
                }

                Optional<Student> existingStudent =
                        studentRepo
                                .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                                        libraryId,
                                        dto.getSeatNumber()
                                );

                if (existingStudent.isPresent()) {

                    failedSeats.add(
                            "Seat " +
                                    dto.getSeatNumber() +
                                    " already occupied"
                    );

                    continue;
                }

                // ==================================================
                // HOLD CHECK
                // ==================================================

                Optional<SeatHold> existingHold =
                        seatHoldRepo
                                .findByLibraryIdAndSeat_SeatNumberAndActiveTrue(
                                        libraryId,
                                        dto.getSeatNumber()
                                );

                if (existingHold.isPresent()) {

                    SeatHold hold =
                            existingHold.get();

                    if (hold.getHoldUntil()
                            .isAfter(LocalDateTime.now())) {

                        failedSeats.add(
                                "Seat " +
                                        dto.getSeatNumber() +
                                        " is currently held for " +
                                        hold.getName()
                        );

                        continue;
                    }

                    /*
                     * Expired hold.
                     */
                    hold.setActive(false);
                    seatHoldRepo.save(hold);
                }

                // ==================================================
                // CREATE STUDENT
                // ==================================================

                Student student = new Student();

                student.setName(dto.getName());

                student.setPhone(dto.getPhone());

                student.setSeat(seat);

                student.setSeatNumber(
                        seat.getSeatNumber()
                );

                student.setAmountPaid(
                        dto.getAmount()
                );

                student.setBookingDate(
                        LocalDate.now()
                );

                student.setStartDate(
                        LocalDateTime.now()
                );

                student.setExpiryDate(
                        dto.getExpiryDate() != null
                                ? dto.getExpiryDate()
                                : LocalDate.now().plusDays(30)
                );

                student.setEndDate(
                        dto.getEndDate() != null
                                ? dto.getEndDate()
                                : LocalDateTime.now().plusDays(30)
                );

                student.setStudentType(
                        dto.getStudentType() != null
                                ? dto.getStudentType()
                                : Student.StudentType.FULL_DAY
                );

                student.setLibrary(library);

                student.setActive(true);

                studentRepo.save(student);

                // ==================================================
                // OCCUPY SEAT
                // ==================================================

                seat.setOccupied(true);

                seatRepo.save(seat);

                bookedSeats.add(
                        "Seat " +
                                dto.getSeatNumber() +
                                " booked"
                );

            } catch (Exception e) {

                failedSeats.add(
                        "Seat " +
                                dto.getSeatNumber() +
                                " failed: " +
                                e.getMessage()
                );
            }
        }

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "success",
                bookedSeats
        );

        response.put(
                "failed",
                failedSeats
        );

        return ResponseEntity.ok(response);
    }
}