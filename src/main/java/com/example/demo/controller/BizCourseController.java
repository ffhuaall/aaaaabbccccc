package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizCourseSchedule;
import com.example.demo.entity.SysUser;
import com.example.demo.service.BizCourseScheduleService;
import com.example.demo.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/course")
public class BizCourseController {

    @Autowired
    private BizCourseScheduleService courseScheduleService;

    // 【新增】引入用户服务，用来查班级
    @Autowired
    private SysUserService userService;

    /**
     * 获取个人周课表
     * 逻辑升级：前端传 studentId -> 后端查 classId -> 返回班级课表
     */
    @GetMapping("/weekly")
    public Result<List<BizCourseSchedule>> getWeeklySchedule(
            @RequestParam Long studentId, 
            @RequestParam Integer week) {
            
        // 1. 先查出这个学生属于哪个班
        SysUser user = userService.getById(studentId);
        
        // 如果学生不存在，或者还没分配班级，直接返回空课表，不报错
        if (user == null || user.getClassId() == null) {
            return Result.success(new ArrayList<>());
        }

        // 2. 根据该学生的班级ID，去查全班通用的课表
        List<BizCourseSchedule> schedule = courseScheduleService.getWeeklyScheduleByClass(user.getClassId(), week);
        
        return Result.success(schedule);
    }
}