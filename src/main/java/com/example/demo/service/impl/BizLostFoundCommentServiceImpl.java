package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.BizLostFoundComment;
import com.example.demo.mapper.BizLostFoundCommentMapper;
import com.example.demo.service.BizLostFoundCommentService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BizLostFoundCommentServiceImpl extends ServiceImpl<BizLostFoundCommentMapper, BizLostFoundComment> implements BizLostFoundCommentService {
    
    @Override
    public List<BizLostFoundComment> listByItemId(Long itemId) {
        QueryWrapper<BizLostFoundComment> wrapper = new QueryWrapper<>();
        wrapper.eq("item_id", itemId).orderByDesc("create_time");
        return this.list(wrapper);
    }
}