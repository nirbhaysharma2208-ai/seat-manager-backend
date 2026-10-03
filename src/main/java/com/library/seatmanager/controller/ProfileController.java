package com.library.seatmanager.controller;

import com.library.seatmanager.dto.ProfileDetailsResponse;
import com.library.seatmanager.dto.UpdateAdminProfileRequest;
import com.library.seatmanager.dto.UpdateLibraryProfileRequest;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Library;
import com.library.seatmanager.entity.Seat;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.LibraryRepository;
import com.library.seatmanager.repository.SeatRepository;
import com.library.seatmanager.service.SecurityService;

import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AdminRepository adminRepo;
    private final LibraryRepository libraryRepo;
    private final SecurityService securityService;
    private final SeatRepository seatRepo;

    public ProfileController(
            AdminRepository adminRepo,
            LibraryRepository libraryRepo,
            SeatRepository seatRepo,
            SecurityService securityService
    ) {
        this.adminRepo = adminRepo;
        this.libraryRepo = libraryRepo;
        this.seatRepo = seatRepo;
        this.securityService = securityService;
    }

    // =========================================================
    // GET PROFILE
    // =========================================================

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/library/{libraryId}")
    public ProfileDetailsResponse getProfile(
            @PathVariable Long libraryId,
            Authentication auth
    ) {

        securityService.validateLibraryAccess(libraryId, auth);

        String userType = securityService.getUserType(auth);

        // ADMIN
        if ("ADMIN".equals(userType)) {

            String phone = auth.getName();

            Admin admin = adminRepo.findByPhone(phone)
                    .orElseThrow(() ->
                            new RuntimeException("Admin not found")
                    );

            Library lib = libraryRepo.findById(libraryId)
                    .orElseThrow(() ->
                            new RuntimeException("Library not found")
                    );

            return new ProfileDetailsResponse(
                    admin.getName(),
                    lib.getTotalSeats(),
                    lib.getLibraryName(),
                    admin.getPhone()
            );
        }

        // EMPLOYEE
        if ("EMPLOYEE".equals(userType)) {

            Library lib = libraryRepo.findById(libraryId)
                    .orElseThrow(() ->
                            new RuntimeException("Library not found")
                    );

            Admin admin = lib.getAdmin();

            if (admin == null) {
                throw new RuntimeException("Library administrator not found");
            }

            return new ProfileDetailsResponse(
                    admin.getName(),
                    lib.getTotalSeats(),
                    lib.getLibraryName(),
                    admin.getPhone()
            );
        }

        throw new RuntimeException("Invalid user type");
    }


    // =========================================================
    // UPDATE ADMIN PROFILE
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin")
    @Transactional
    public ProfileDetailsResponse updateAdminProfile(
            @RequestBody UpdateAdminProfileRequest request,
            Authentication auth
    ) {

        String currentPhone = auth.getName();

        Admin admin = adminRepo.findByPhone(currentPhone)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );

        // -------------------------
        // Validate name
        // -------------------------

        if (request.getName() == null ||
                request.getName().trim().isEmpty()) {

            throw new RuntimeException("Admin name is required");
        }

        // -------------------------
        // Update ONLY name
        // -------------------------

        admin.setName(request.getName().trim());

        adminRepo.save(admin);

        // -------------------------
        // Return profile
        // -------------------------

        Library library = admin.getLibraries()
                .stream()
                .findFirst()
                .orElse(null);

        if (library == null) {

            return new ProfileDetailsResponse(
                    admin.getName(),
                    0,
                    null,
                    admin.getPhone()
            );
        }

        return new ProfileDetailsResponse(
                admin.getName(),
                library.getTotalSeats(),
                library.getLibraryName(),
                admin.getPhone()
        );
    }


    // =========================================================
    // UPDATE LIBRARY PROFILE
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/library/{libraryId}")
    @Transactional
    public ProfileDetailsResponse updateLibraryProfile(
            @PathVariable Long libraryId,
            @RequestBody UpdateLibraryProfileRequest request,
            Authentication auth
    ) {

        // =========================================================
        // VALIDATE ACCESS
        // =========================================================

        securityService.validateLibraryAccess(libraryId, auth);

        String currentPhone = auth.getName();

        Admin admin = adminRepo.findByPhone(currentPhone)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );

        // =========================================================
        // LOAD LIBRARY
        // =========================================================

        Library library = libraryRepo.findById(libraryId)
                .orElseThrow(() ->
                        new RuntimeException("Library not found")
                );

        // =========================================================
        // VERIFY OWNER
        // =========================================================

        if (library.getAdmin() == null ||
                !library.getAdmin().getId().equals(admin.getId())) {

            throw new RuntimeException(
                    "You are not authorized to update this library"
            );
        }

        // =========================================================
        // VALIDATE LIBRARY NAME
        // =========================================================

        if (request.getLibraryName() == null ||
                request.getLibraryName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Library name is required"
            );
        }

        // =========================================================
        // VALIDATE SEATS
        // =========================================================

        if (request.getTotalSeats() == null ||
                request.getTotalSeats() <= 0) {

            throw new RuntimeException(
                    "Total seats must be greater than 0"
            );
        }

        int oldTotalSeats = library.getTotalSeats();
        int newTotalSeats = request.getTotalSeats();

        // =========================================================
        // HANDLE SEAT COUNT CHANGE
        // =========================================================

        if (newTotalSeats < oldTotalSeats) {

            List<Seat> seatsToRemove =
                    seatRepo.findByLibraryIdAndSeatNumberGreaterThan(
                            libraryId,
                            newTotalSeats
                    );

            // Check whether any seat above the new limit is occupied
            boolean occupiedSeatExists =
                    seatsToRemove.stream()
                            .anyMatch(Seat::isOccupied);

            if (occupiedSeatExists) {
                throw new RuntimeException(
                        "Cannot reduce seats because one or more seats above the new limit are occupied."
                );
            }

            // Delete vacant seats above the new limit
            if (!seatsToRemove.isEmpty()) {
                seatRepo.deleteAll(seatsToRemove);
            }
        }

        // =========================================================
        // CREATE NEW SEATS
        // =========================================================

        if (newTotalSeats > oldTotalSeats) {

            List<Seat> existingSeats =
                    seatRepo.findByLibraryId(libraryId);

            java.util.Set<Integer> existingSeatNumbers =
                    existingSeats.stream()
                            .map(Seat::getSeatNumber)
                            .collect(java.util.stream.Collectors.toSet());

            for (int seatNumber = 1;
                 seatNumber <= newTotalSeats;
                 seatNumber++) {

                if (!existingSeatNumbers.contains(seatNumber)) {

                    Seat seat = new Seat();

                    seat.setSeatNumber(seatNumber);
                    seat.setOccupied(false);
                    seat.setLibrary(library);

                    seatRepo.save(seat);
                }
            }
        }

        // =========================================================
        // UPDATE LIBRARY
        // =========================================================

        library.setLibraryName(
                request.getLibraryName().trim()
        );

        library.setTotalSeats(newTotalSeats);

        libraryRepo.save(library);

        // =========================================================
        // RETURN UPDATED PROFILE
        // =========================================================

        return new ProfileDetailsResponse(
                admin.getName(),
                library.getTotalSeats(),
                library.getLibraryName(),
                admin.getPhone()
        );
    }
}