package com.library.seatmanager.controller;

import com.library.seatmanager.dto.CreateEmployeeRequest;
import com.library.seatmanager.dto.EmployeeResponse;
import com.library.seatmanager.dto.UpdateEmployeeRequest;
import com.library.seatmanager.service.EmployeeService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;


    public EmployeeController(
            EmployeeService employeeService
    ) {
        this.employeeService = employeeService;
    }


    // =========================================================
    // CREATE EMPLOYEE
    // =========================================================

    @PostMapping("/library/{libraryId}")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @PathVariable Long libraryId,
            @RequestBody CreateEmployeeRequest request,
            Authentication authentication
    ) {

        String adminPhone = authentication.getName();

        EmployeeResponse response =
                employeeService.createEmployee(
                        libraryId,
                        request,
                        adminPhone
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // GET ALL EMPLOYEES
    // =========================================================

    @GetMapping("/library/{libraryId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployees(
            @PathVariable Long libraryId,
            Authentication authentication
    ) {

        String adminPhone = authentication.getName();

        List<EmployeeResponse> employees =
                employeeService.getEmployees(
                        libraryId,
                        adminPhone
                );

        return ResponseEntity.ok(employees);
    }


    // =========================================================
    // GET SINGLE EMPLOYEE
    // =========================================================

    @GetMapping("/{employeeId}/library/{libraryId}")
    public ResponseEntity<EmployeeResponse> getEmployee(
            @PathVariable Long employeeId,
            @PathVariable Long libraryId,
            Authentication authentication
    ) {

        String adminPhone = authentication.getName();

        EmployeeResponse response =
                employeeService.getEmployee(
                        employeeId,
                        libraryId,
                        adminPhone
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // UPDATE EMPLOYEE
    // =========================================================

    @PutMapping("/{employeeId}/library/{libraryId}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long employeeId,
            @PathVariable Long libraryId,
            @RequestBody UpdateEmployeeRequest request,
            Authentication authentication
    ) {

        String adminPhone = authentication.getName();

        EmployeeResponse response =
                employeeService.updateEmployee(
                        employeeId,
                        libraryId,
                        request,
                        adminPhone
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // ACTIVATE / DEACTIVATE
    // =========================================================

    @PatchMapping("/{employeeId}/library/{libraryId}/status")
    public ResponseEntity<EmployeeResponse> updateStatus(
            @PathVariable Long employeeId,
            @PathVariable Long libraryId,
            @RequestParam boolean active,
            Authentication authentication
    ) {

        String adminPhone = authentication.getName();

        EmployeeResponse response =
                employeeService.updateStatus(
                        employeeId,
                        libraryId,
                        active,
                        adminPhone
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // DELETE EMPLOYEE
    // =========================================================

    @DeleteMapping("/{employeeId}/library/{libraryId}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Long employeeId,
            @PathVariable Long libraryId,
            Authentication authentication
    ) {

        String adminPhone = authentication.getName();

        employeeService.deleteEmployee(
                employeeId,
                libraryId,
                adminPhone
        );

        return ResponseEntity.ok(
                "Employee deleted successfully"
        );
    }
}