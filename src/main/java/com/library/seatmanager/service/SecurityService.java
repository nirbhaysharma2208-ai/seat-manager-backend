package com.library.seatmanager.service;

import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Library;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.LibraryRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SecurityService {

    private final AdminRepository adminRepository;
    private final LibraryRepository libraryRepository;

    public SecurityService(
            AdminRepository adminRepository,
            LibraryRepository libraryRepository
    ) {
        this.adminRepository = adminRepository;
        this.libraryRepository = libraryRepository;
    }

    /**
     * Validates that the currently authenticated user
     * has access to the requested library.
     */
    public void validateLibraryAccess(
            Long libraryId,
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        Object details = authentication.getDetails();

        if (!(details instanceof Claims claims)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authentication details"
            );
        }

        String userType =
                claims.get("userType", String.class);

        // ==========================================
        // ADMIN
        // ==========================================

        if ("ADMIN".equals(userType)) {

            String phone = authentication.getName();

            Admin admin = adminRepository
                    .findByPhone(phone)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.UNAUTHORIZED,
                                    "Admin not found"
                            )
                    );

            Library library = libraryRepository
                    .findById(libraryId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Library not found"
                            )
                    );

            if (library.getAdmin() == null
                    || !library.getAdmin()
                    .getId()
                    .equals(admin.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You do not have access to this library"
                );
            }

            return;
        }

        // ==========================================
        // EMPLOYEE
        // ==========================================

        if ("EMPLOYEE".equals(userType)) {

            Long employeeLibraryId =
                    claims.get("libraryId", Long.class);

            if (employeeLibraryId == null) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Employee library information missing"
                );
            }

            if (!employeeLibraryId.equals(libraryId)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You do not have access to this library"
                );
            }

            return;
        }

        // ==========================================
        // UNKNOWN USER TYPE
        // ==========================================

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Invalid user type"
        );
    }


    /**
     * Returns the library ID from the authenticated
     * employee JWT.
     */
    public Long getAuthenticatedLibraryId(
            Authentication authentication
    ) {

        if (authentication == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        Object details = authentication.getDetails();

        if (!(details instanceof Claims claims)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authentication details"
            );
        }

        return claims.get("libraryId", Long.class);
    }


    /**
     * Returns the logged-in user's type.
     */
    public String getUserType(
            Authentication authentication
    ) {

        if (authentication == null) {
            return null;
        }

        Object details = authentication.getDetails();

        if (!(details instanceof Claims claims)) {
            return null;
        }

        return claims.get("userType", String.class);
    }
}