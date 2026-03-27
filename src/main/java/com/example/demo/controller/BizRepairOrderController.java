package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    // 1. 学生提交报修工单
    @PostMapping("/submit")
    public Result<Boolean> submit(@RequestBody BizRepairOrder order) {
        // 模拟当前登录学生ID，后续从 Token 中解析
        if (order.getStudentId() == null) order.setStudentId(1001L); 
        return Result.success(repairOrderService.submitOrder(order));
    }

    // 2. 查询所有工单 (大厅列表)
    @GetMapping("/list")
    public Result<List<BizRepairOrder>> list() {
        return Result.success(repairOrderService.list());
    }

    // 3. 维修工接单 (通过 URL 传参传递 orderId)
    @PostMapping("/take/{orderId}")
    public Result<Boolean> takeOrder(@PathVariable Long orderId) {
        // 模拟当前登录维修工ID
        Long currentWorkerId = 2001L; 
        boolean success = repairOrderService.takeOrder(orderId, currentWorkerId);
        return success ? Result.success(true) : Result.error(400, "接单失败，该工单可能已被抢走或状态不正确");
    }

    // 4. 维修工完成维修
    @PostMapping("/finish/{orderId}")
    public Result<Boolean> finishRepair(@PathVariable Long orderId) {
        boolean success = repairOrderService.finishRepair(orderId);
        return success ? Result.success(true) : Result.error(400, "操作失败，工单状态不正确");
    }
}