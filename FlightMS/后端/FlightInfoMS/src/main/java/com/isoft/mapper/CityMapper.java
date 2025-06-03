package com.isoft.mapper;

import com.isoft.entity.City;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CityMapper {
    @Select("SELECT * FROM cityinfo")
    List<City> findAll();

    @Select("SELECT * FROM cityinfo WHERE cityid = #{cityid}")
    City findById(@Param("cityid") Integer cityid);
}