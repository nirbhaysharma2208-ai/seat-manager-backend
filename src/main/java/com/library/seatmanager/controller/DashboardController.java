package com.library.seatmanager.controller;

import com.library.seatmanager.dto.DashboardResponse;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Student;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.SeatRepository;
import com.library.seatmanager.repository.StudentRepository;
import com.library.seatmanager.service.DashboardService;
import com.library.seatmanager.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;



@RestController
@RequestMapping("/api")
@CrossOrigin
public class DashboardController {

    @Autowired
    private DashboardService service;

    @Autowired
    private SeatRepository seatRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private SecurityService securityService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'ACCOUNTANT')")
    @GetMapping("/dashboards/{libraryId}")
    public DashboardResponse getDashboard(
            Authentication authentication,
            @PathVariable Long libraryId) {

        // 🔐 Verify user belongs to this library
        securityService.validateLibraryAccess(
                libraryId,
                authentication
        );

        try {

            int totalSeats =
                    seatRepo.countByLibraryId(libraryId);

            long filledSeats =
                    studentRepo
                            .countBySeat_Library_IdAndStudentTypeAndActiveTrue(
                                    libraryId,
                                    Student.StudentType.FULL_DAY
                            );

            long halfDayCount =
                    studentRepo
                            .countByLibrary_IdAndStudentTypeAndActiveTrue(
                                    libraryId,
                                    Student.StudentType.HALF_DAY
                            );

            long vacantSeats =
                    totalSeats - filledSeats;

            return new DashboardResponse(
                    totalSeats,
                    filledSeats,
                    vacantSeats,
                    halfDayCount
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error fetching dashboard data: "
                            + e.getMessage()
            );
        }
    }
}

