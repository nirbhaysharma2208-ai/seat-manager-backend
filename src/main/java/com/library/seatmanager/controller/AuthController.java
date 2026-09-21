package com.library.seatmanager.controller;

import com.library.seatmanager.config.JwtUtil;
import com.library.seatmanager.dto.EmployeeLoginRequest;
import com.library.seatmanager.dto.LoginRequest;
import com.library.seatmanager.dto.LoginResponse;
import com.library.seatmanager.dto.SignupRequest;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Employee;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;


    // =========================================================
    // ADMIN SIGNUP
    // =========================================================

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody SignupRequest req) {

        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (req.getName() == null ||
                req.getName().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Name is required");
        }

        if (req.getPhone() == null ||
                req.getPhone().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone number is required");
        }

        if (req.getPassword() == null ||
                req.getPassword().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Password is required");
        }


        // -----------------------------------------------------
        // CHECK DUPLICATE PHONE
        // -----------------------------------------------------

        String phone = req.getPhone().trim();

        if (adminRepo.findByPhone(phone).isPresent()) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone already registered");
        }


        // -----------------------------------------------------
        // CREATE ADMIN
        // -----------------------------------------------------

        Admin admin = new Admin();

        admin.setName(req.getName().trim());

        admin.setPhone(phone);

        admin.setPassword(
                encoder.encode(req.getPassword())
        );

        adminRepo.save(admin);


        // -----------------------------------------------------
        // GENERATE ADMIN JWT
        // -----------------------------------------------------

        String token =
                jwtUtil.generateAdminToken(admin);


        // -----------------------------------------------------
        // RETURN COMPLETE AUTH RESPONSE
        // -----------------------------------------------------

        Map<String, Object> response =
                new HashMap<>();

        response.put("token", token);

        response.put("userType", "ADMIN");

        response.put("userId", admin.getId());

        response.put("name", admin.getName());


        /*
         * IMPORTANT:
         *
         * A newly registered admin normally doesn't
         * have a library yet.
         *
         * Therefore libraryId is intentionally NOT
         * included here.
         *
         * AuthContext will then send the admin to
         * CreateLibraryScreen.
         */


        return ResponseEntity.ok(response);
    }


    // =========================================================
    // ADMIN LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req) {

        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (req.getPhone() == null ||
                req.getPhone().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone number is required");
        }

        if (req.getPassword() == null ||
                req.getPassword().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Password is required");
        }


        // -----------------------------------------------------
        // FIND ADMIN
        // -----------------------------------------------------

        String phone = req.getPhone().trim();

        Admin admin =
                adminRepo.findByPhone(phone)
                        .orElse(null);


        if (admin == null) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid credentials");
        }


        // -----------------------------------------------------
        // CHECK PASSWORD
        // -----------------------------------------------------

        if (!encoder.matches(
                req.getPassword(),
                admin.getPassword()
        )) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid credentials");
        }


        // -----------------------------------------------------
        // GENERATE TOKEN
        // -----------------------------------------------------

        String token =
                jwtUtil.generateAdminToken(admin);


        // -----------------------------------------------------
        // FIND ADMIN LIBRARY
        // -----------------------------------------------------

        Long libraryId = null;
        String libraryName = null;

        if (admin.getLibraries() != null &&
                !admin.getLibraries().isEmpty()) {

            /*
             * Admin currently may have multiple libraries.
             *
             * For the existing application flow,
             * use the first library.
             */

            var library =
                    admin.getLibraries().get(0);

            if (library != null) {

                libraryId = library.getId();

                libraryName = library.getLibraryName();
            }
        }


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        Map<String, Object> response =
                new HashMap<>();

        response.put("token", token);

        response.put("userType", "ADMIN");

        response.put("userId", admin.getId());

        response.put("name", admin.getName());

        if (libraryId != null) {
            response.put("libraryId", libraryId);
        }

        if (libraryName != null) {
            response.put("libraryName", libraryName);
        }


        return ResponseEntity.ok(response);
    }


    // =========================================================
    // EMPLOYEE LOGIN
    // =========================================================

    @PostMapping("/employee-login")
    public ResponseEntity<?> employeeLogin(
            @RequestBody EmployeeLoginRequest req) {

        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (req.getUsername() == null ||
                req.getUsername().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Username is required");
        }

        if (req.getPassword() == null ||
                req.getPassword().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Password is required");
        }


        // -----------------------------------------------------
        // FIND EMPLOYEE
        // -----------------------------------------------------

        Employee employee =
                employeeRepo
                        .findByUsername(
                                req.getUsername().trim()
                        )
                        .orElse(null);


        if (employee == null) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid username or password");
        }


        // -----------------------------------------------------
        // CHECK ACTIVE STATUS
        // -----------------------------------------------------

        if (!employee.isActive()) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "Your employee account is inactive. " +
                                    "Please contact your library administrator."
                    );
        }


        // -----------------------------------------------------
        // CHECK PASSWORD
        // -----------------------------------------------------

        if (!encoder.matches(
                req.getPassword(),
                employee.getPassword()
        )) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid username or password");
        }


        // -----------------------------------------------------
        // CHECK LIBRARY
        // -----------------------------------------------------

        if (employee.getLibrary() == null ||
                employee.getLibrary().getId() == null) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "Employee is not assigned to a library."
                    );
        }


        // -----------------------------------------------------
        // GENERATE TOKEN
        // -----------------------------------------------------

        String token =
                jwtUtil.generateEmployeeToken(employee);


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        LoginResponse response =
                new LoginResponse(
                        token,
                        "EMPLOYEE",
                        employee.getId(),
                        employee.getLibrary().getId(),
                        employee.getRole().name(),
                        employee.getName()
                );


        return ResponseEntity.ok(response);
    }
}