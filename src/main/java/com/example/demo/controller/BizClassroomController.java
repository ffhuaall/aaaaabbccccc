package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.BizCourseSchedule;
import com.example.demo.mapper.BizCourseScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/classroom")
public class BizClassroomController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BizCourseScheduleMapper courseScheduleMapper;

    // 假设学校所有的自习教室清单
    private static final List<String> ALL_CLASSROOMS = Arrays.asList(
            "教1-101", "教1-102", "教1-201", "教1-202",
            "教2-101", "教2-304", "实验楼-302", "实验楼-401", "机房-501"
    );

    /**
     * 高并发场景：查询某天某节课的空闲教室 (改版：支持真实日期)
     */
    @GetMapping("/idle")
    public Result<List<String>> getIdleClassrooms(@RequestParam String date, @RequestParam Integer period) {
        
        // 1. 将前端传来的 "2024-05-20" 自动转换为星期几 (1=周一, 7=周日)
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int dayOfWeek = localDate.getDayOfWeek().getValue();

        // 2. 缓存的 Key 现在基于具体的日期和节次
        String redisKey = "idle_classroom:date:" + date + ":period:" + period;

        // 3. 先查 Redis 缓存
        String cachedData = redisTemplate.opsForValue().get(redisKey);
        if (cachedData != null) {
            List<String> idleRooms = JSON.parseArray(cachedData, String.class);
            return Result.success(idleRooms);
        }

        // 4. Redis 未命中，穿透查询 MySQL 数据库
        QueryWrapper<BizCourseSchedule> wrapper = new QueryWrapper<>();
        wrapper.eq("day_of_week", dayOfWeek).eq("period", period);
               
        List<BizCourseSchedule> busySchedules = courseScheduleMapper.selectList(wrapper);
        
        // 提取被占用的教室名
        List<String> busyRooms = busySchedules.stream()
                .map(BizCourseSchedule::getLocation)
                .collect(Collectors.toList());

        // 5. 计算差集：所有教室 - 被占用教室 = 空闲教室
        List<String> idleRooms = new ArrayList<>(ALL_CLASSROOMS);
        idleRooms.removeAll(busyRooms);

        // 6. 存入 Redis，并设置 1 小时过期
        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(idleRooms), 1, TimeUnit.HOURS);

        return Result.success(idleRooms);
    }
}