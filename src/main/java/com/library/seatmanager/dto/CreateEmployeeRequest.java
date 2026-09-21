package com.library.seatmanager.dto;

import com.library.seatmanager.entity.EmployeeRole;

public class CreateEmployeeRequest {

    private String name;
    private String phone;
    private String username;
    private String password;
    private EmployeeRole role;


    public CreateEmployeeRequest() {
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


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }
}