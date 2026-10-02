package com.library.seatmanager.controller;

import com.library.seatmanager.dto.ProfileDetailsResponse;
import com.library.seatmanager.dto.UpdateAdminProfileRequest;
import com.library.seatmanager.dto.UpdateLibraryProfileRequest;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Library;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.LibraryRepository;
import com.library.seatmanager.service.SecurityService;

import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AdminRepository adminRepo;
    private final LibraryRepository libraryRepo;
    private final SecurityService securityService;

    public ProfileController(
            AdminRepository adminRepo,
            LibraryRepository libraryRepo,
            SecurityService securityService
    ) {
        this.adminRepo = adminRepo;
        this.libraryRepo = libraryRepo;
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
        // Validate phone
        // -------------------------

        if (request.getPhone() == null ||
                request.getPhone().trim().isEmpty()) {

            throw new RuntimeException("Phone number is required");
        }

        String newPhone = request.getPhone().trim();

        if (!newPhone.matches("\\d{10}")) {
            throw new RuntimeException(
                    "Phone number must contain exactly 10 digits"
            );
        }

        // -------------------------
        // Check duplicate phone
        // -------------------------

        if (!newPhone.equals(admin.getPhone())) {

            adminRepo.findByPhone(newPhone)
                    .ifPresent(existingAdmin -> {

                        if (!existingAdmin.getId().equals(admin.getId())) {
                            throw new RuntimeException(
                                    "Phone number is already registered"
                            );
                        }
                    });
        }

        // -------------------------
        // Update admin
        // -------------------------

        admin.setName(request.getName().trim());
        admin.setPhone(newPhone);

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

        // -------------------------
        // Validate access
        // -------------------------

        securityService.validateLibraryAccess(libraryId, auth);

        String currentPhone = auth.getName();

        Admin admin = adminRepo.findByPhone(currentPhone)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );

        // -------------------------
        // Load library
        // -------------------------

        Library library = libraryRepo.findById(libraryId)
                .orElseThrow(() ->
                        new RuntimeException("Library not found")
                );

        // -------------------------
        // Ensure this admin owns
        // the library
        // -------------------------

        if (library.getAdmin() == null ||
                !library.getAdmin().getId().equals(admin.getId())) {

            throw new RuntimeException(
                    "You are not authorized to update this library"
            );
        }

        // -------------------------
        // Validate library name
        // -------------------------

        if (request.getLibraryName() == null ||
                request.getLibraryName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Library name is required"
            );
        }

        // -------------------------
        // Validate seats
        // -------------------------

        if (request.getTotalSeats() == null ||
                request.getTotalSeats() <= 0) {

            throw new RuntimeException(
                    "Total seats must be greater than 0"
            );
        }

        // -------------------------
        // Update library
        // -------------------------

        library.setLibraryName(
                request.getLibraryName().trim()
        );

        library.setTotalSeats(
                request.getTotalSeats()
        );

        libraryRepo.save(library);

        // -------------------------
        // Return updated profile
        // -------------------------

        return new ProfileDetailsResponse(
                admin.getName(),
                library.getTotalSeats(),
                library.getLibraryName(),
                admin.getPhone()
        );
    }
}