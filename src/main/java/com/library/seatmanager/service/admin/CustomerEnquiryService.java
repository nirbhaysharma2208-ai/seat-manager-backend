package com.library.seatmanager.service.admin;


import com.library.seatmanager.dto.admin.CustomerEnquiryRequest;
import com.library.seatmanager.dto.admin.CustomerEnquiryResponse;
import com.library.seatmanager.dto.admin.EnquiryStatus;
import com.library.seatmanager.entity.admin.CustomerEnquiry;
import com.library.seatmanager.repository.admin.CustomerEnquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerEnquiryService {

    private final CustomerEnquiryRepository repository;

    public CustomerEnquiryResponse createEnquiry(
            CustomerEnquiryRequest request) {

        CustomerEnquiry enquiry = CustomerEnquiry.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .libraryName(request.getLibrary())
                .city(request.getCity())
                .seats(request.getSeats())
                .currentManagement(request.getCurrent())
                .preferredCallTime(request.getTime())
                .message(request.getMessage())
                .consent(request.getConsent())
                .status(EnquiryStatus.NEW)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CustomerEnquiry saved = repository.save(enquiry);

        return mapToResponse(saved);
    }

    private CustomerEnquiryResponse mapToResponse(CustomerEnquiry saved) {
        return CustomerEnquiryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .phone(saved.getPhone())
                .email(saved.getEmail())
                .libraryName(saved.getLibraryName())
                .city(saved.getCity())
                .seats(saved.getSeats())
                .currentManagement(saved.getCurrentManagement())
                .preferredCallTime(saved.getPreferredCallTime())
                .message(saved.getMessage())
                .consent(saved.getConsent())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}