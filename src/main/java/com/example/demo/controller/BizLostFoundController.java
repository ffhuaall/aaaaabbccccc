package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.document.EsLostFound;
import com.example.demo.entity.BizLostFound;
import com.example.demo.entity.BizLostFoundComment;
import com.example.demo.entity.SysMessage;
import com.example.demo.mapper.SysMessageMapper;
import com.example.demo.service.BizLostFoundCommentService;
import com.example.demo.service.BizLostFoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lost-found")
public class BizLostFoundController {

    @Autowired
    private BizLostFoundService lostFoundService;

    @Autowired
    private SysMessageMapper messageMapper;

    @Autowired
    private BizLostFoundCommentService commentService;
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

    /**
     * 1. 认领/找回物品 (点击确认认领时调用)
     * @param id 物品ID
     * @param claimerId 认领人ID
     */
    @PostMapping("/claim/{id}")
    public Result<Boolean> claimItem(@PathVariable Long id, @RequestParam Long claimerId) {
        BizLostFound item = lostFoundService.getById(id);
        if (item != null && item.getStatus() == 0) {
            // 更新物品状态为已解决 (1)
            item.setStatus(1);
            lostFoundService.updateById(item);

            // 【核心】给发布者发送系统通知
            SysMessage msg = new SysMessage();
            msg.setReceiverId(item.getPublisherId()); // 接收者是发布人
            String typeText = item.getType() == 0 ? "寻物" : "招领";
            msg.setTitle("失物招领模块提醒");
            msg.setContent("同学你好！你发布的" + typeText + "信息【" + item.getItemName() + "】已被其他同学点击认领/确认。请及时进入模块查看并联系！");
            msg.setType("LOST_FOUND"); // 消息类型
            msg.setIsRead(0);
            msg.setCreateTime(java.time.LocalDateTime.now());
            // 假设你有一个 messageMapper 或者直接用 messageService
            messageMapper.insert(msg); 

            return Result.success(true);
        }
        return Result.error(400, "操作失败：物品可能已被他人认领");
    }

    /**
     * 2. [新增] 留言功能：为物品添加评论/留言
     */
    @PostMapping("/comment/add")
    public Result<Boolean> addComment(@RequestBody BizLostFoundComment comment) {
        // 需新建对应的实体类和数据库表，此处省略具体 Service 实现，逻辑为 insert
        return Result.success(commentService.save(comment));
    }

    /**
     * 3. [新增] 获取某件物品的所有留言
     */
    @GetMapping("/comments/{itemId}")
    public Result<List<BizLostFoundComment>> getComments(@PathVariable Long itemId) {
        return Result.success(commentService.listByItemId(itemId));
    }
}