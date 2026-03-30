package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizCourseSchedule;
import com.example.demo.service.BizCourseScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/course")
public class BizCourseController {

    @Autowired
    private BizCourseScheduleService courseScheduleService;

    /**
     * 获取个人周课表
     * 示例: GET /course/weekly?studentId=1001&week=5
     */
    @GetMapping("/weekly")
    public Result<List<BizCourseSchedule>> getWeeklySchedule(
            @RequestParam Long studentId, 
            @RequestParam Integer week) {
            
        List<BizCourseSchedule> schedule = courseScheduleService.getWeeklySchedule(studentId, week);
        return Result.success(schedule);
    }
}