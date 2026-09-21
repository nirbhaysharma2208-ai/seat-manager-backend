package com.library.seatmanager.dto;



public class HoldRequest {

    private Integer days;

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return "HoldRequest{" +
                "days=" + days +
                '}';
    }
}