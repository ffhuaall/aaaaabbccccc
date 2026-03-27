package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}