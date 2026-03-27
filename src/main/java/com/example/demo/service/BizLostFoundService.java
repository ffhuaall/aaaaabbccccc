package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.document.EsLostFound;
import com.example.demo.entity.BizLostFound;

import java.util.List;

public interface BizLostFoundService extends IService<BizLostFound> {
    
    /**
     * 发布失物招领（落盘 MySQL 并同步至 ES）
     */
    boolean publishLostFound(BizLostFound lostFound);

    /**
     * 基于 ES 的关键字全文检索
     */
    List<EsLostFound> searchLostFound(String keyword);
}