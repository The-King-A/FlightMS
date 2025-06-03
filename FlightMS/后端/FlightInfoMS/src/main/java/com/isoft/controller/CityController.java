package com.isoft.controller;

import com.isoft.entity.City;
import com.isoft.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin(origins = "http://localhost:5173") // 指定前端开发服务器地址
public class CityController {
    @Autowired
    private CityService cityService;

    @GetMapping
    public List<City> getAllCities() {
        return cityService.findAllCities();
    }

    @GetMapping("/{id}")
    public City getCityById(@PathVariable("id") Integer cityid) {
        return cityService.findCityById(cityid);
    }
}