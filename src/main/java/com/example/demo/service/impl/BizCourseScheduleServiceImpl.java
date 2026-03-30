package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.BizCourseSchedule;
import com.example.demo.mapper.BizCourseScheduleMapper;
import com.example.demo.service.BizCourseScheduleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BizCourseScheduleServiceImpl extends ServiceImpl<BizCourseScheduleMapper, BizCourseSchedule> implements BizCourseScheduleService {

    @Override
    public List<BizCourseSchedule> getWeeklySchedule(Long studentId, Integer targetWeek) {
        // 1. 查询该学生名下的所有课程排期
        QueryWrapper<BizCourseSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        List<BizCourseSchedule> allCourses = this.list(queryWrapper);

        // 2. 使用 Java 8 Stream 流进行强大的单双周与周区间过滤
        return allCourses.stream().filter(course -> {
            // 规则 A：查询的周次必须在课程的起始周和结束周之间
            if (targetWeek < course.getStartWeek() || targetWeek > course.getEndWeek()) {
                return false;
            }
            
            // 规则 B：处理单双周逻辑 (0-每周, 1-单周, 2-双周)
            int weekType = course.getWeekType();
            if (weekType == 1 && targetWeek % 2 == 0) {
                return false; // 课程要求单周上，但查询的是双周，过滤掉
            }
            if (weekType == 2 && targetWeek % 2 != 0) {
                return false; // 课程要求双周上，但查询的是单周，过滤掉
            }
            
            // 满足所有条件，保留该课程
            return true;
        }).collect(Collectors.toList());
    }
}