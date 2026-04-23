package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.BizLostFoundComment;
import java.util.List;

public interface BizLostFoundCommentService extends IService<BizLostFoundComment> {
    List<BizLostFoundComment> listByItemId(Long itemId);
}