package com.example.demo.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.BizActivity;

public interface BizActivityService extends IService<BizActivity> {
    public List<BizActivity> recommendActivities(Long targetUserId, int topN);

}