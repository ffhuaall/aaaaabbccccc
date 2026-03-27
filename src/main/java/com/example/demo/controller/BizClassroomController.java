package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.IdleClassroomDTO;
import com.example.demo.service.BizClassroomScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/classroom")
public class BizClassroomController {

    @Autowired
    private BizClassroomScheduleService scheduleService;

    /**
     * 查询空闲教室
     * 示例请求: GET /classroom/idle?date=2026-05-20&period=3
     */
    @GetMapping("/idle")
    public Result<List<IdleClassroomDTO>> getIdleClassrooms(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Integer period) {
        
        List<IdleClassroomDTO> list = scheduleService.getIdleClassrooms(date, period);
        return Result.success(list);
    }
}