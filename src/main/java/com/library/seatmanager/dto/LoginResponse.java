package com.library.seatmanager.dto;

public class LoginResponse {

    private String token;
    private String userType;
    private Long userId;
    private Long libraryId;
    private String role;
    private String name;


    public LoginResponse() {
    }


    public LoginResponse(
            String token,
            String userType,
            Long userId,
            Long libraryId,
            String role,
            String name
    ) {

        this.token = token;
        this.userType = userType;
        this.userId = userId;
        this.libraryId = libraryId;
        this.role = role;
        this.name = name;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}