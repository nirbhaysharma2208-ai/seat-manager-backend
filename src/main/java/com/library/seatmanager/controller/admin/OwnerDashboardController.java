package com.library.seatmanager.controller.admin;


import com.library.seatmanager.dto.admin.OwnerDashboardResponse;
import com.library.seatmanager.service.admin.OwnerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerDashboardController {

    private final OwnerDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerDashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                dashboardService.getDashboard()
        );
    }
}