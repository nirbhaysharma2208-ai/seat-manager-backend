package com.library.seatmanager.dto.admin;


import java.time.LocalDateTime;

public class CustomerEnquiryResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String libraryName;
    private String city;
    private String seats;
    private String currentManagement;
    private String preferredCallTime;
    private String message;
    private Boolean consent;
    private EnquiryStatus status;
    private LocalDateTime createdAt;

    public CustomerEnquiryResponse() {
    }

    private CustomerEnquiryResponse(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.phone = builder.phone;
        this.email = builder.email;
        this.libraryName = builder.libraryName;
        this.city = builder.city;
        this.seats = builder.seats;
        this.currentManagement = builder.currentManagement;
        this.preferredCallTime = builder.preferredCallTime;
        this.message = builder.message;
        this.consent = builder.consent;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private String name;
        private String phone;
        private String email;
        private String libraryName;
        private String city;
        private String seats;
        private String currentManagement;
        private String preferredCallTime;
        private String message;
        private Boolean consent;
        private EnquiryStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder libraryName(String libraryName) {
            this.libraryName = libraryName;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder seats(String seats) {
            this.seats = seats;
            return this;
        }

        public Builder currentManagement(String currentManagement) {
            this.currentManagement = currentManagement;
            return this;
        }

        public Builder preferredCallTime(String preferredCallTime) {
            this.preferredCallTime = preferredCallTime;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder consent(Boolean consent) {
            this.consent = consent;
            return this;
        }

        public Builder status(EnquiryStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CustomerEnquiryResponse build() {
            return new CustomerEnquiryResponse(this);
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getCurrentManagement() {
        return currentManagement;
    }

    public void setCurrentManagement(String currentManagement) {
        this.currentManagement = currentManagement;
    }

    public String getPreferredCallTime() {
        return preferredCallTime;
    }

    public void setPreferredCallTime(String preferredCallTime) {
        this.preferredCallTime = preferredCallTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getConsent() {
        return consent;
    }

    public void setConsent(Boolean consent) {
        this.consent = consent;
    }

    public EnquiryStatus getStatus() {
        return status;
    }

    public void setStatus(EnquiryStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}