package com.library.seatmanager.controller.admin;



import com.library.seatmanager.dto.admin.CustomerEnquiryRequest;
import com.library.seatmanager.dto.admin.CustomerEnquiryResponse;
import com.library.seatmanager.dto.admin.EnquiryStatus;
import com.library.seatmanager.entity.admin.CustomerEnquiry;
import com.library.seatmanager.repository.admin.CustomerEnquiryRepository;
import com.library.seatmanager.service.admin.CustomerEnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-enquiries")
@RequiredArgsConstructor
@CrossOrigin
public class CustomerEnquiryController {

    @Autowired
    private CustomerEnquiryService enquiryService;

    @Autowired
    private CustomerEnquiryRepository repository;

    @PostMapping
    public ResponseEntity<CustomerEnquiryResponse> createEnquiry(
            @RequestBody CustomerEnquiryRequest request) {

        CustomerEnquiryResponse response =
                enquiryService.createEnquiry(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<CustomerEnquiry>> getAllEnquiries() {

        return ResponseEntity.ok(
                repository.findAll()
        );
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> updateEnquiryStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String statusValue = request.get("status");

        if (statusValue == null || statusValue.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Status is required"));
        }

        final EnquiryStatus status;

        try {
            status = EnquiryStatus.valueOf(
                    statusValue.trim().toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid enquiry status"));
        }

        CustomerEnquiry enquiry = repository.findById(id)
                .orElse(null);

        if (enquiry == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Enquiry not found"));
        }

        enquiry.setStatus(status);
        CustomerEnquiry saved = repository.save(enquiry);

        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "status", saved.getStatus().name()
        ));
    }


}