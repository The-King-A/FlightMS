package com.isoft.dto;

import com.isoft.entity.Flight;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FlightDTO extends Flight {
    private String startCityName;
    private String endCityName;
}