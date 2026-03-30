package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_course_schedule")
public class BizCourseSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String courseName;
    private String teacherName;
    private String location;
    private Integer dayOfWeek;
    private Integer period;
    private Integer startWeek;
    private Integer endWeek;
    private Integer weekType;
}