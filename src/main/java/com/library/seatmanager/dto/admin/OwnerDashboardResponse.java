package com.library.seatmanager.dto.admin;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDashboardResponse {

    private long totalCustomers;

    private long activeCustomers;

    private long suspendedCustomers;

    private long totalLibraries;

    private long totalSeats;

    private long occupiedSeats;

    private long vacantSeats;

    private long pendingEnquiries;

    private long convertedEnquiries;

    private long activeSubscriptions;

    private long expiredSubscriptions;

    private double monthlyRevenue;
}