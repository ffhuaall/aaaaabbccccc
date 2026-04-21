package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统公告 Mapper 接口
 * 继承 BaseMapper 后，MyBatis-Plus 会自动帮我们生成所有基础的 CRUD 方法
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
}