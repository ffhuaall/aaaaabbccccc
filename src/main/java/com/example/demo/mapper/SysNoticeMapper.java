package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;

//系统公告 Mapper 接口
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
}