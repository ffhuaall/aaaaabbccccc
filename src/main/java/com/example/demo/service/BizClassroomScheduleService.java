package com.example.demo.service;

import com.example.demo.dto.IdleClassroomDTO;
import java.time.LocalDate;
import java.util.List;

public interface BizClassroomScheduleService {
    // 高并发查询空闲教室接口
    List<IdleClassroomDTO> getIdleClassrooms(LocalDate date, Integer period);
}