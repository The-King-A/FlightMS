package com.isoft.service;

import com.isoft.entity.Flight;
import com.isoft.dto.FlightDTO;
import com.isoft.mapper.FlightMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightService {
    @Autowired
    private FlightMapper flightMapper;

    // CityMapper is not used in this service class
    // @Autowired
    // private CityMapper cityMapper;

    public List<Flight> findAllFlights() {
        return flightMapper.findAll();
    }

    public Flight findFlightById(Integer flightid) {
        return flightMapper.findById(flightid);
    }

    public int addFlight(Flight flight) {
        return flightMapper.insert(flight);
    }

    public int updateFlight(Flight flight) {
        return flightMapper.update(flight);
    }

    public int deleteFlight(Integer flightid) {
        return flightMapper.delete(flightid);
    }

    public List<FlightDTO> findAllFlightsWithCityNames() {
        return flightMapper.findAllWithCityNames();
    }

    public FlightDTO findFlightByIdWithCityNames(Integer flightid) {
        return flightMapper.findByIdWithCityNames(flightid);
    }
}