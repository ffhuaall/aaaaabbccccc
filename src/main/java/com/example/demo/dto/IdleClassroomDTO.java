package com.example.demo.dto;

import lombok.Data;

@Data
public class IdleClassroomDTO {
    private String buildingName; // 教学楼名称
    private String roomNumber;   // 教室门牌号
    private Integer capacity;    // 容纳人数
}