package com.library.seatmanager.dto.admin;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerEnquiryRequest {


    private String name;


    private String phone;

    private String email;


    private String library;


    private String city;


    private String seats;

    private String current;

    private String time;

    private String message;

    private Boolean consent;
}