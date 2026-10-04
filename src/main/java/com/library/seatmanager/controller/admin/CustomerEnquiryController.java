package com.library.seatmanager.controller.admin;



import com.library.seatmanager.dto.admin.CustomerEnquiryRequest;
import com.library.seatmanager.dto.admin.CustomerEnquiryResponse;
import com.library.seatmanager.entity.admin.CustomerEnquiry;
import com.library.seatmanager.repository.admin.CustomerEnquiryRepository;
import com.library.seatmanager.service.admin.CustomerEnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<CustomerEnquiry>> getAllEnquiries() {

        return ResponseEntity.ok(
                repository.findAll()
        );
    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<CustomerEnquiryResponse> getEnquiry(
//            @PathVariable Long id) {
//
//        return ResponseEntity.ok(
//                enquiryService.getEnquiryById(id)
//        );
//    }
//
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<CustomerEnquiryResponse> updateStatus(
//            @PathVariable Long id,
//            @RequestParam EnquiryStatus status) {
//
//        return ResponseEntity.ok(
//                enquiryService.updateStatus(id, status)
//        );
//    }
}