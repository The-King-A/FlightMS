package com.isoft.controller;

import com.isoft.entity.Flight;
import com.isoft.dto.FlightDTO;
import com.isoft.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "http://localhost:5173") // 指定前端开发服务器地址
public class FlightController {
    @Autowired
    private FlightService flightService;

    @GetMapping
    public List<Flight> getAllFlights() {
        return flightService.findAllFlights();
    }

    @GetMapping("/{id}")
    public Flight getFlightById(@PathVariable("id") Integer flightid) {
        return flightService.findFlightById(flightid);
    }

    @PostMapping
    public int addFlight(@RequestBody Flight flight) {
        return flightService.addFlight(flight);
    }

    @PutMapping("/{id}")
    public int updateFlight(@PathVariable("id") Integer flightid, @RequestBody Flight flight) {
        flight.setFlightid(flightid);
        return flightService.updateFlight(flight);
    }

    @DeleteMapping("/{id}")
    public int deleteFlight(@PathVariable("id") Integer flightid) {
        return flightService.deleteFlight(flightid);
    }

    @GetMapping("/with-city-names")
    public List<FlightDTO> getAllFlightsWithCityNames() {
        return flightService.findAllFlightsWithCityNames();
    }

    @GetMapping("/{id}/with-city-names")
    public FlightDTO getFlightByIdWithCityNames(@PathVariable("id") Integer flightid) {
        return flightService.findFlightByIdWithCityNames(flightid);
    }
}
