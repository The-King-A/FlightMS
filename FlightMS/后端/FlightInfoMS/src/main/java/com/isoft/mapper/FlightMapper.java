package com.isoft.mapper;

import com.isoft.entity.Flight;
import com.isoft.dto.FlightDTO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FlightMapper {
        @Select("SELECT * FROM flightinfo")
        List<Flight> findAll();

        @Select("SELECT * FROM flightinfo WHERE flightid = #{flightid}")
        Flight findById(@Param("flightid") Integer flightid);

        @Insert("INSERT INTO flightinfo(flightnum, flydate, starttime, flytime, startcity, endcity, seatnum) " +
                        "VALUES(#{flightnum}, #{flydate}, #{starttime}, #{flytime}, #{startcity}, #{endcity}, #{seatnum})")
        @Options(useGeneratedKeys = true, keyProperty = "flightid")
        int insert(Flight flight);

        @Update("UPDATE flightinfo SET flightnum=#{flightnum}, flydate=#{flydate}, starttime=#{starttime}, " +
                        "flytime=#{flytime}, startcity=#{startcity}, endcity=#{endcity}, seatnum=#{seatnum} " +
                        "WHERE flightid=#{flightid}")
        int update(Flight flight);

        @Delete("DELETE FROM flightinfo WHERE flightid=#{flightid}")
        int delete(@Param("flightid") Integer flightid);

        @Select("SELECT f.*, sc.cityname as startCityName, ec.cityname as endCityName " +
                        "FROM flightinfo f " +
                        "LEFT JOIN cityinfo sc ON f.startcity = sc.cityid " +
                        "LEFT JOIN cityinfo ec ON f.endcity = ec.cityid")
        List<FlightDTO> findAllWithCityNames();

        @Select("SELECT f.*, sc.cityname as startCityName, ec.cityname as endCityName " +
                        "FROM flightinfo f " +
                        "LEFT JOIN cityinfo sc ON f.startcity = sc.cityid " +
                        "LEFT JOIN cityinfo ec ON f.endcity = ec.cityid " +
                        "WHERE f.flightid = #{flightid}")
        FlightDTO findByIdWithCityNames(@Param("flightid") Integer flightid);
}
