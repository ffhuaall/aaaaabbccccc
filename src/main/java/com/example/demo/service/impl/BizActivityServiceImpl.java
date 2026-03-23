package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.BizActivityMapper;
import com.example.demo.entity.BizActivity;
import com.example.demo.service.BizActivityService;
import org.springframework.stereotype.Service;

@Service
public class BizActivityServiceImpl extends ServiceImpl<BizActivityMapper, BizActivity> implements BizActivityService {
}