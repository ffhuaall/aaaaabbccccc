package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.SysMessage;
import com.example.demo.mapper.SysMessageMapper;
import com.example.demo.service.SysMessageService;
import org.springframework.stereotype.Service;

@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {
}