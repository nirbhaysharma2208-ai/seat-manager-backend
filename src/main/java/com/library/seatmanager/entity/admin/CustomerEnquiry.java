package com.library.seatmanager.entity.admin;


import com.library.seatmanager.dto.admin.EnquiryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_enquiries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phone;

    private String email;

    private String libraryName;

    private String city;

    private String seats;

    private String currentManagement;

    private String preferredCallTime;

    @Column(length = 2000)
    private String message;

    private Boolean consent;

    @Enumerated(EnumType.STRING)
    private EnquiryStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String source;

    private String assignedTo;

    @Column(length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {

        if (status == null) {
            status = EnquiryStatus.NEW;
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}