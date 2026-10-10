package com.library.seatmanager.service.admin;


import com.library.seatmanager.dto.AccountStatus;
import com.library.seatmanager.dto.SubscriptionStatus;
import com.library.seatmanager.dto.admin.EnquiryStatus;
import com.library.seatmanager.dto.admin.OwnerDashboardResponse;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.LibraryRepository;
import com.library.seatmanager.repository.admin.CustomerEnquiryRepository;
import com.library.seatmanager.repository.admin.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService {

    private final AdminRepository adminRepository;
    private final LibraryRepository libraryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CustomerEnquiryRepository enquiryRepository;

    public OwnerDashboardResponse getDashboard() {

        List<Admin> admins = adminRepository.findAll();

        long totalCustomers = admins.stream()
                .filter(admin -> admin.getRole() != null
                        && admin.getRole().name().equals("ADMIN"))
                .count();

        long activeCustomers = admins.stream()
                .filter(admin ->
                        admin.getRole() != null
                                && admin.getRole().name().equals("ADMIN")
                                && admin.getStatus() == AccountStatus.ACTIVE)
                .count();

        long suspendedCustomers = admins.stream()
                .filter(admin ->
                        admin.getRole() != null
                                && admin.getRole().name().equals("ADMIN")
                                && admin.getStatus() == AccountStatus.SUSPENDED)
                .count();

        long totalLibraries = libraryRepository.count();

        long totalSeats = libraryRepository.findAll()
                .stream()
                .mapToLong(library -> library.getTotalSeats())
                .sum();

        long pendingEnquiries = enquiryRepository
                .findAll()
                .stream()
                .filter(e -> e.getStatus() == EnquiryStatus.NEW
                        || e.getStatus() == EnquiryStatus.CONTACTED)
                .count();

        long convertedEnquiries = enquiryRepository
                .findAll()
                .stream()
                .filter(e -> e.getStatus() == EnquiryStatus.CONVERTED)
                .count();

        long activeSubscriptions =
                subscriptionRepository
                        .countByStatus(SubscriptionStatus.ACTIVE);

        long expiredSubscriptions =
                subscriptionRepository
                        .countByStatus(SubscriptionStatus.EXPIRED);

        return OwnerDashboardResponse.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .suspendedCustomers(suspendedCustomers)
                .totalLibraries(totalLibraries)
                .totalSeats(totalSeats)
                .occupiedSeats(0)
                .vacantSeats(totalSeats)
                .pendingEnquiries(pendingEnquiries)
                .convertedEnquiries(convertedEnquiries)
                .activeSubscriptions(activeSubscriptions)
                .expiredSubscriptions(expiredSubscriptions)
                .monthlyRevenue(0)
                .build();
    }
}