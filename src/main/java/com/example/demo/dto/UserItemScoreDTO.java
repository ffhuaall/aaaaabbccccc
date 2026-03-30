package com.example.demo.dto;

import lombok.Data;

@Data
public class UserItemScoreDTO {
    private Long userId;
    private Long activityId;
    private Double score; // 综合评分
}