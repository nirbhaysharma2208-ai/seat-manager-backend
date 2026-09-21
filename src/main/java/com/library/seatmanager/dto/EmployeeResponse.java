package com.library.seatmanager.dto;

import com.library.seatmanager.entity.Employee;
import com.library.seatmanager.entity.EmployeeRole;

public class EmployeeResponse {

    private Long id;
    private String name;
    private String phone;
    private String username;
    private EmployeeRole role;
    private boolean active;


    public EmployeeResponse() {
    }


    public EmployeeResponse(
            Long id,
            String name,
            String phone,
            String username,
            EmployeeRole role,
            boolean active
    ) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.username = username;
        this.role = role;
        this.active = active;
    }


    public static EmployeeResponse from(Employee employee) {

        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getPhone(),
                employee.getUsername(),
                employee.getRole(),
                employee.isActive()
        );
    }


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


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}