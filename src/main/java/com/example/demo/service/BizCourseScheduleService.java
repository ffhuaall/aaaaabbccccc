package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.BizCourseSchedule;
import java.util.List;

public interface BizCourseScheduleService extends IService<BizCourseSchedule> {
    /**
     * 查询指定学生在指定周次的课表
     */
    List<BizCourseSchedule> getWeeklySchedule(Long studentId, Integer targetWeek);
}