package com.example.demo.service;

// import com.example.demo.dto.IdleClassroomDTO;
// import com.example.demo.entity.BizClassroom;

// import java.time.LocalDate;
// import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.BizClassroom;
import java.util.List;

public interface BizClassroomScheduleService {
    // 高并发查询空闲教室接口
    List<BizClassroom> getIdleClassrooms(String campus, String building, Integer week, Integer dayOfWeek, Integer period);

}