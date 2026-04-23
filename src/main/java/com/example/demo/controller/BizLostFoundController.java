package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.document.EsLostFound;
import com.example.demo.entity.BizLostFound;
import com.example.demo.service.BizLostFoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lost-found")
public class BizLostFoundController {

    @Autowired
    private BizLostFoundService lostFoundService;

    /**
     * 获取失物招领列表大厅 (前端 fetchList 调用的就是这里)
     */
    @GetMapping("/list")
    public Result<List<BizLostFound>> getList() {
        // 按照发布时间倒序排列，最新的在最前面
        QueryWrapper<BizLostFound> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return Result.success(lostFoundService.list(wrapper));
    }

    /**
     * 发布失物/招领信息
     */
    @PostMapping("/publish")
    public Result<Boolean> publish(@RequestBody BizLostFound lostFound) {
        // [填坑] 实际开发中，publisherId 应该从 JWT Token 中解析出来。
        // 这里为了让前端能顺利跑通测试，如果前端没传，默认给它分配测试 ID = 1001 (张三的ID)
        if (lostFound.getPublisherId() == null) {
            lostFound.setPublisherId(1001L);
        }
        boolean success = lostFoundService.publishLostFound(lostFound);
        return Result.success(success);
    }

    /**
     * 关键字全文搜索 (调用 ES)
     */
    @GetMapping("/search")
    public Result<List<EsLostFound>> search(@RequestParam String keyword) {
        List<EsLostFound> result = lostFoundService.searchLostFound(keyword);
        return Result.success(result);
    }

    /**
     * 标记失物招领为已结案（找到了/归还了）
     */
    @PostMapping("/resolve/{id}")
    public Result<Boolean> resolveItem(@PathVariable Long id) {
        BizLostFound item = lostFoundService.getById(id);
        if (item != null) {
            item.setStatus(1); // 假设 1 代表已结案/已找到
            lostFoundService.updateById(item);
            return Result.success(true);
        }
        return Result.error(400, "记录不存在");
    }

    /**
     * 删除失物招领记录
     */
    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteItem(@PathVariable Long id) {
        boolean success = lostFoundService.removeById(id);
        return Result.success(success);
    }
}