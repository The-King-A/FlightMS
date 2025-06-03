package com.isoft.service;

import com.isoft.entity.City;
import com.isoft.mapper.CityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {
    @Autowired
    private CityMapper cityMapper;

    public List<City> findAllCities() {
        return cityMapper.findAll();
    }

    public City findCityById(Integer cityid) {
        return cityMapper.findById(cityid);
    }
}