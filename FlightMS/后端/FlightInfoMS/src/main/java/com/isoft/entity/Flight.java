package com.isoft.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Flight {
    private Integer flightid;
    private String flightnum;
    private Date flydate;
    private String starttime;
    private String flytime;
    private Integer startcity;
    private Integer endcity;
    private Integer seatnum;
}