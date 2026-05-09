package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.document.EsLostFound;
import com.example.demo.entity.BizLostFound;
import com.example.demo.mapper.BizLostFoundMapper;
import com.example.demo.repository.EsLostFoundRepository;
import com.example.demo.service.BizLostFoundService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BizLostFoundServiceImpl extends ServiceImpl<BizLostFoundMapper, BizLostFound> implements BizLostFoundService {

    //注入ES仓库接口
    @Autowired
    private EsLostFoundRepository esRepository;

    @Override
    @Transactional(rollbackFor = Exception.class) //开启数据库事务
    public boolean publishLostFound(BizLostFound lostFound) {
        //初始化基础数据
        if (lostFound.getCreateTime() == null) {
            lostFound.setCreateTime(LocalDateTime.now());
        }
        lostFound.setStatus(0); //0代表寻找中

        //保存到 MySQL 数据库
        boolean success = this.save(lostFound);

        //MySQL写入成功后同步保存到 Elasticsearch
        if (success) {
            EsLostFound esDoc = new EsLostFound();
            //使用 Spring 提供的 BeanUtils 快速拷贝同名属性
            BeanUtils.copyProperties(lostFound, esDoc);
            //存入 ES 建立全文索引
            esRepository.save(esDoc);
        }
        
        return success;
    }

    @Override
    public List<EsLostFound> searchLostFound(String keyword) {
        //获取包含高亮信息的 SearchHits
        SearchHits<EsLostFound> searchHits = esRepository.findByItemNameOrDescription(keyword, keyword);
        
        List<EsLostFound> resultList = new ArrayList<>();
        
        //遍历每一个搜索命中结果
        for (SearchHit<EsLostFound> hit : searchHits) {
            //获取原本的实体数据
            EsLostFound content = hit.getContent();
            
            //提取并替换 itemName 的高亮片段
            List<String> highlightItemName = hit.getHighlightField("itemName");
            if (highlightItemName != null && !highlightItemName.isEmpty()) {
                //因为是单字段，取第0个元素即为高亮后的完整字符串
                content.setItemName(highlightItemName.get(0));
            }
            
            //提取并替换 description 的高亮片段
            List<String> highlightDesc = hit.getHighlightField("description");
            if (highlightDesc != null && !highlightDesc.isEmpty()) {
                content.setDescription(highlightDesc.get(0));
            }
            
            resultList.add(content);
        }
        
        return resultList;
    }
}