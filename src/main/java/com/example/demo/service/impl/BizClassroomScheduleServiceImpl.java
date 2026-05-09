package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.BizClassroom;
import com.example.demo.entity.BizCourseSchedule;
import com.example.demo.mapper.BizClassroomMapper;
import com.example.demo.service.BizClassroomScheduleService;
import com.example.demo.service.BizCourseScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BizClassroomScheduleServiceImpl extends ServiceImpl<BizClassroomMapper, BizClassroom> implements BizClassroomScheduleService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BizCourseScheduleService courseScheduleService;

    @Override
    public List<BizClassroom> getIdleClassrooms(String campus, String building, Integer week, Integer dayOfWeek, Integer period) {
        //构造 Redis Key
        String redisKey = String.format("idle_room:%s:%s:w%s:d%s:p%s", 
            campus == null ? "all" : campus, 
            building == null ? "all" : building, 
            week == null ? "x" : week, 
            dayOfWeek == null ? "x" : dayOfWeek, 
            period == null ? "x" : period);

        //尝试获取缓存
        String cachedData = redisTemplate.opsForValue().get(redisKey);
        if (cachedData != null) {
            System.out.println("【Redis 命中】极速返回数据 -> " + redisKey);
            return JSON.parseArray(cachedData, BizClassroom.class);
        }

        System.out.println("【Redis 未命中】执行差集算法 -> " + redisKey);

        //查全集
        QueryWrapper<BizClassroom> roomWrapper = new QueryWrapper<>();
        if (campus != null) roomWrapper.eq("campus", campus);
        if (building != null) roomWrapper.eq("building", building);
        List<BizClassroom> allRooms = this.list(roomWrapper);

        //逻辑处理，如果不具备时间三元素，直接返回物理全集
        if (week == null || dayOfWeek == null || period == null) {
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(allRooms), 12, TimeUnit.HOURS);
            return allRooms;
        }

        //核心差集算法：查占用集并过滤
        QueryWrapper<BizCourseSchedule> courseWrapper = new QueryWrapper<>();
        courseWrapper.eq("day_of_week", dayOfWeek).eq("period", period);
        if (campus != null && building != null) {
            courseWrapper.like("location", campus + "-" + building);
        }
        
        List<BizCourseSchedule> rawSchedules = courseScheduleService.list(courseWrapper);

        //应用单双周、起止周流式过滤
        List<String> busyLocations = rawSchedules.stream().filter(course -> {
            if (week < course.getStartWeek() || week > course.getEndWeek()) return false;
            int weekType = course.getWeekType();
            if (weekType == 1 && week % 2 == 0) return false;
            if (weekType == 2 && week % 2 != 0) return false;
            return true;
        }).map(BizCourseSchedule::getLocation).collect(Collectors.toList());

        //计算差集
        List<BizClassroom> idleRooms = allRooms.stream().filter(room -> {
            String standardName = room.getCampus() + "-" + room.getBuilding() + "-" + room.getRoomNo();
            return !busyLocations.contains(standardName);
        }).collect(Collectors.toList());

        //写入缓存并返回
        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(idleRooms), 12, TimeUnit.HOURS);
        return idleRooms;
    }
}