package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizClassroom;
import com.example.demo.service.BizClassroomScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/classroom")
public class BizClassroomController {

    @Autowired
    private BizClassroomScheduleService classroomService;

    @GetMapping("/idle")
    public Result<List<BizClassroom>> getIdleClassrooms(
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(required = false) Integer period) {

        // Controller 就像一个经理，把接到的参数直接丢给 Service 这个技术专家去处理
        List<BizClassroom> idleRooms = classroomService.getIdleClassrooms(campus, building, week, dayOfWeek, period);
        
        return Result.success(idleRooms);
    }
}