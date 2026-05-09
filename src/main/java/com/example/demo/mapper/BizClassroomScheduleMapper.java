package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.IdleClassroomDTO;
import com.example.demo.entity.BizClassroomSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BizClassroomScheduleMapper extends BaseMapper<BizClassroomSchedule> {

    //自定义多表联查 SQL：根据日期和节次，查询空闲的教室信息
    @Select("SELECT c.building_name, c.room_number, c.capacity " +
            "FROM biz_classroom_schedule s " +
            "JOIN biz_classroom c ON s.classroom_id = c.id " +
            "WHERE s.query_date = #{queryDate} AND s.period = #{period} AND s.is_free = 1")
    List<IdleClassroomDTO> findIdleClassrooms(@Param("queryDate") LocalDate queryDate, @Param("period") Integer period);
}