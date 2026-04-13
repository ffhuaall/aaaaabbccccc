package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizRepairOrder;
import com.example.demo.service.BizRepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair")
public class BizRepairOrderController {

    @Autowired
    private BizRepairOrderService repairOrderService;

    // 获取所有报修工单 (管理员看所有，学生前端已做过滤)
    @GetMapping("/list")
    public Result<List<BizRepairOrder>> getList() {
        return Result.success(repairOrderService.list());
    }

    // 学生提交报修 (你之前可能写过，保持原样即可)
    @PostMapping("/submit")
    public Result<Boolean> submit(@RequestBody BizRepairOrder order) {
        order.setStatus(0); // 0-待接单
        order.setCreateTime(java.time.LocalDateTime.now());
        return Result.success(repairOrderService.save(order));
    }

    /**
     * 【新增】管理员接单接口 (状态 0 -> 1)
     */
    @PostMapping("/take/{id}")
    public Result<Boolean> takeOrder(@PathVariable Long id) {
        BizRepairOrder order = repairOrderService.getById(id);
        if (order != null && order.getStatus() == 0) {
            order.setStatus(1); // 1-维修中
            repairOrderService.updateById(order);
            return Result.success(true);
        }
        return Result.error(400, "接单失败，该工单状态不正确");
    }

    /**
     * 【新增】管理员完工接口 (状态 1 -> 2)
     */
    @PostMapping("/finish/{id}")
    public Result<Boolean> finishOrder(@PathVariable Long id) {
        BizRepairOrder order = repairOrderService.getById(id);
        if (order != null && order.getStatus() == 1) {
            order.setStatus(2); // 2-待评价
            repairOrderService.updateById(order);
            return Result.success(true);
        }
        return Result.error(400, "操作失败，该工单尚未开始维修");
    }
}