package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.BizClassroom;
import com.example.demo.entity.BizCourseSchedule;
import com.example.demo.mapper.BizClassroomMapper;
import com.example.demo.service.BizCourseScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/classroom")
public class BizClassroomController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BizClassroomMapper classroomMapper;

    @Autowired
    private BizCourseScheduleService courseScheduleService;

    /**
     * 高可用空教室查询：引入校区、教学楼维度，并使用 Redis 抗并发
     */
    @GetMapping("/idle")
public Result<List<BizClassroom>> getIdleClassrooms(
        @RequestParam(required = false) String campus,
        @RequestParam(required = false) String building,
        @RequestParam(required = false) Integer week,
        @RequestParam(required = false) Integer dayOfWeek,
        @RequestParam(required = false) Integer period) {

    // 1. 动态构造 Redis Key，缺失的参数用 "_" 代替
    String redisKey = String.format("idle_room:%s:%s:w%s:d%s:p%s", 
        campus == null ? "all" : campus, 
        building == null ? "all" : building, 
        week == null ? "x" : week, 
        dayOfWeek == null ? "x" : dayOfWeek, 
        period == null ? "x" : period);

    String cachedData = redisTemplate.opsForValue().get(redisKey);
    if (cachedData != null) {
        return Result.success(JSON.parseArray(cachedData, BizClassroom.class));
    }

    // 2. 查询物理教室全集 (动态添加条件)
    QueryWrapper<BizClassroom> roomWrapper = new QueryWrapper<>();
    if (campus != null) roomWrapper.eq("campus", campus);
    if (building != null) roomWrapper.eq("building", building);
    List<BizClassroom> allRooms = classroomMapper.selectList(roomWrapper);

    // 3. 如果没有提供完整的【时间三元素】，则认为用户只是想看该区域有哪些教室，不进行占用剔除
    if (week == null || dayOfWeek == null || period == null) {
        // 直接存入 Redis 并返回全集
        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(allRooms), 12, TimeUnit.HOURS);
        return Result.success(allRooms);
    }

    // 4. 如果时间参数完整，则执行原有的“差集算法”
    QueryWrapper<BizCourseSchedule> courseWrapper = new QueryWrapper<>();
    courseWrapper.eq("day_of_week", dayOfWeek).eq("period", period);
    if (campus != null && building != null) {
        courseWrapper.like("location", campus + "-" + building);
    }
    
    List<BizCourseSchedule> rawSchedules = courseScheduleService.list(courseWrapper);

    List<String> busyLocations = rawSchedules.stream().filter(course -> {
        if (week < course.getStartWeek() || week > course.getEndWeek()) return false;
        int weekType = course.getWeekType();
        if (weekType == 1 && week % 2 == 0) return false;
        if (weekType == 2 && week % 2 != 0) return false;
        return true;
    }).map(BizCourseSchedule::getLocation).collect(Collectors.toList());

    List<BizClassroom> idleRooms = allRooms.stream().filter(room -> {
        String standardName = room.getCampus() + "-" + room.getBuilding() + "-" + room.getRoomNo();
        return !busyLocations.contains(standardName);
    }).collect(Collectors.toList());

    redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(idleRooms), 12, TimeUnit.HOURS);
    return Result.success(idleRooms);
  }
}