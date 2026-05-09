package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.document.EsLostFound;
import com.example.demo.entity.BizLostFound;
import com.example.demo.entity.BizLostFoundComment;
import com.example.demo.entity.SysLog;
import com.example.demo.entity.SysMessage;
import com.example.demo.mapper.SysLogMapper;
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

    @Autowired
    private SysLogMapper logMapper;

    //获取失物招领列表大厅
    @GetMapping("/list")
    public Result<List<BizLostFound>> getList() {
        //按照发布时间倒序排列，最新的在最前面
        QueryWrapper<BizLostFound> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return Result.success(lostFoundService.list(wrapper));
    }

    //发布失物/招领信息
    @PostMapping("/publish")
    public Result<Boolean> publish(@RequestBody BizLostFound lostFound) {
        if (lostFound.getPublisherId() == null) {
            lostFound.setPublisherId(1001L);
        }
        boolean success = lostFoundService.publishLostFound(lostFound);
        return Result.success(success);
    }

    //关键字es全文搜索
    @GetMapping("/search")
    public Result<List<EsLostFound>> search(@RequestParam String keyword) {
        List<EsLostFound> result = lostFoundService.searchLostFound(keyword);
        return Result.success(result);
    }

    //标记失物招领为已完成
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

    // 删除失物招领记录
    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteItem(@PathVariable Long id) {
        BizLostFound item = lostFoundService.getById(id);
        boolean success = lostFoundService.removeById(id);

        if (success && item != null) {
            SysLog log = new SysLog();
            log.setUsername("superadmin");
            log.setModule("失物招领");
            log.setAction("物理删除");
            log.setType("danger");
            log.setDetail("删除了失物招领记录：【" + item.getItemName() + "】");
            logMapper.insert(log);
        }
        
        return Result.success(success);
    }

    //认领物品
    @PostMapping("/claim/{id}")
    public Result<Boolean> claimItem(@PathVariable Long id, @RequestParam Long claimerId) {
        BizLostFound item = lostFoundService.getById(id);
        if (item != null && item.getStatus() == 0) {
            item.setStatus(1);
            lostFoundService.updateById(item);

            SysMessage msg = new SysMessage();
            msg.setReceiverId(item.getPublisherId());
            String typeText = item.getType() == 0 ? "寻物" : "招领";
            msg.setTitle("失物招领模块提醒");
            msg.setContent("同学你好！你发布的" + typeText + "信息【" + item.getItemName() + "】已被其他同学点击认领/确认。请及时进入模块查看并联系！");
            msg.setType("LOST_FOUND");
            msg.setIsRead(0);
            msg.setCreateTime(java.time.LocalDateTime.now());
            messageMapper.insert(msg); 

            return Result.success(true);
        }
        return Result.error(400, "操作失败：物品可能已被他人认领");
    }

    //留言功能：为物品添加评论/留言
    @PostMapping("/comment/add")
    public Result<Boolean> addComment(@RequestBody BizLostFoundComment comment) {
        return Result.success(commentService.save(comment));
    }

    //获取某件物品的所有留言
    @GetMapping("/comments/{itemId}")
    public Result<List<BizLostFoundComment>> getComments(@PathVariable Long itemId) {
        return Result.success(commentService.listByItemId(itemId));
    }

    //强制下架失物招领信息
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancelItem(@PathVariable Long id) {
        BizLostFound item = lostFoundService.getById(id);
        if (item != null) {
            item.setStatus(-1); 
            lostFoundService.updateById(item);

            SysMessage msg = new SysMessage();
            msg.setReceiverId(item.getPublisherId()); 
            msg.setTitle("系统管理通知");
            msg.setContent("同学你好！你发布的失物招领信息【" + item.getItemName() + "】由于违规或其他原因，已被管理员强制下架作废。");
            msg.setType("LOST_FOUND"); 
            msg.setIsRead(0);
            msg.setCreateTime(java.time.LocalDateTime.now());
            messageMapper.insert(msg);

            SysLog log = new SysLog();
            log.setUsername("superadmin");
            log.setModule("失物招领");
            log.setAction("强制下架");
            log.setType("warning");
            log.setDetail("判定违规，强制下架了帖子：【" + item.getItemName() + "】");
            logMapper.insert(log);

            return Result.success(true);
        }
        return Result.error(400, "操作失败：该物品记录不存在");
    }
}