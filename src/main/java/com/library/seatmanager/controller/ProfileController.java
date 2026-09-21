package com.library.seatmanager.controller;

import com.library.seatmanager.dto.ProfileDetailsResponse;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Library;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.LibraryRepository;
import com.library.seatmanager.service.SecurityService;

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
    // ADMIN + EMPLOYEES
    // =========================================================

    @PreAuthorize(
            "hasRole('ADMIN') or hasRole('EMPLOYEE')"
    )
    @GetMapping("/library/{libraryId}")
    public ProfileDetailsResponse getProfile(
            @PathVariable Long libraryId,
            Authentication auth
    ) {

        // -----------------------------------------------------
        // IMPORTANT:
        // This validates that the logged-in user actually
        // belongs to this library.
        // -----------------------------------------------------

        securityService.validateLibraryAccess(
                libraryId,
                auth
        );

        String userType =
                securityService.getUserType(auth);

        // =====================================================
        // ADMIN
        // =====================================================

        if ("ADMIN".equals(userType)) {

            String phone = auth.getName();

            Admin admin = adminRepo
                    .findByPhone(phone)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Admin not found"
                            )
                    );

            Library lib = libraryRepo
                    .findById(libraryId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Library not found"
                            )
                    );

            return new ProfileDetailsResponse(
                    admin.getName(),
                    lib.getTotalSeats(),
                    lib.getLibraryName(),
                    admin.getPhone()
            );
        }

        // =====================================================
        // EMPLOYEE
        // =====================================================

        if ("EMPLOYEE".equals(userType)) {

            Library lib = libraryRepo
                    .findById(libraryId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Library not found"
                            )
                    );

            /*
             * Employee does not own the library.
             *
             * We therefore return the library information,
             * while the existing SecurityService guarantees
             * that the employee belongs to this library.
             *
             * Admin-specific information is not available
             * through the employee JWT, so we fetch the
             * library owner.
             */

            Admin admin = lib.getAdmin();

            if (admin == null) {
                throw new RuntimeException(
                        "Library administrator not found"
                );
            }

            return new ProfileDetailsResponse(
                    admin.getName(),
                    lib.getTotalSeats(),
                    lib.getLibraryName(),
                    admin.getPhone()
            );
        }

        throw new RuntimeException(
                "Invalid user type"
        );
    }
}