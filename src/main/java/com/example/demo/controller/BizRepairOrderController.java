package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizRepairEvaluation;
import com.example.demo.entity.BizRepairOrder;
import com.example.demo.entity.SysMessage;
import com.example.demo.service.BizRepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair")
public class BizRepairOrderController {

    @Autowired
    private com.example.demo.mapper.SysMessageMapper messageMapper;
    @Autowired
    private BizRepairOrderService repairOrderService;
    @Autowired
    private com.example.demo.mapper.BizRepairEvaluationMapper evaluationMapper;

    //获取所有报修工单
    @GetMapping("/list")
    public Result<List<BizRepairOrder>> getList() {
        return Result.success(repairOrderService.list());
    }

    //学生提交报修
    @PostMapping("/submit")
    public Result<Boolean> submit(@RequestBody BizRepairOrder order) {
        order.setStatus(0); // 0-待接单
        order.setCreateTime(java.time.LocalDateTime.now());
        return Result.success(repairOrderService.save(order));
    }

    //学生撤销报修单(状态为 -1)
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        BizRepairOrder order = repairOrderService.getById(id);
        //只有状态为0的才能撤销，如果已经接单就不能撤了
        if (order != null && order.getStatus() == 0) {
            order.setStatus(-1); //已撤销
            repairOrderService.updateById(order);
            return Result.success(true);
        }
        return Result.error(400, "撤销失败，工单可能已被师傅接单处理");
    }

    //学生提交评价
    @PostMapping("/evaluate")
    public Result<Boolean> evaluateOrder(@RequestBody BizRepairEvaluation evaluation) {
        BizRepairOrder order = repairOrderService.getById(evaluation.getOrderId());
        if (order != null && order.getStatus() == 2) {
            //保存评价
            evaluation.setCreateTime(java.time.LocalDateTime.now());
            evaluationMapper.insert(evaluation);
            
            //更新工单状态为已完成
            order.setStatus(3);
            repairOrderService.updateById(order);

            //给当时接单的师傅发通知
            if (order.getWorkerId() != null) {
                SysMessage msg = new SysMessage();
                msg.setReceiverId(order.getWorkerId());
                msg.setTitle("收到服务评价反馈");
                msg.setContent("辛苦了！您处理的报修单【" + order.getTitle() + "】已收到学生的评价（" + evaluation.getScore() + "星）。");
                msg.setType("REPAIR");
                msg.setIsRead(0);
                msg.setCreateTime(java.time.LocalDateTime.now());
                messageMapper.insert(msg);
            }

            return Result.success(true);
        }
        return Result.error(400, "评价失败，工单状态不正确");
    }

    @GetMapping("/evaluation/{orderId}")
    public Result<BizRepairEvaluation> getEvaluation(@PathVariable Long orderId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BizRepairEvaluation> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return Result.success(evaluationMapper.selectOne(wrapper));
    }

    //管理员接单接口(状态 0 -> 1)
    @PostMapping("/take/{id}")
    public Result<Boolean> takeOrder(@PathVariable Long id, @RequestParam Long workerId) {
        BizRepairOrder order = repairOrderService.getById(id);
        if (order != null && order.getStatus() == 0) {
            order.setStatus(1); //1-维修中
            order.setWorkerId(workerId); //绑定是哪位师傅接的单
            repairOrderService.updateById(order);

            //给学生发通知
            SysMessage msg = new SysMessage();
            msg.setReceiverId(order.getStudentId());
            msg.setTitle("报修进度更新");
            msg.setContent("好消息！您的报修单【" + order.getTitle() + "】已被后勤师傅接手，即将为您上门处理。");
            msg.setType("REPAIR");
            msg.setIsRead(0);
            msg.setCreateTime(java.time.LocalDateTime.now());
            messageMapper.insert(msg);

            return Result.success(true);
        }
        return Result.error(400, "接单失败，该工单状态不正确");
    }

    //管理员完工接口 (状态 1 -> 2)
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

    //删除工单
    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteOrder(@PathVariable Long id) {
        return Result.success(repairOrderService.removeById(id));
    }

    /**
     *派单
     * @param orderId 工单ID
     * @param workerId 维修人员用户ID
     */
    @PostMapping("/assign")
    public Result<Boolean> assignOrder(@RequestParam Long orderId, @RequestParam Long workerId) {
        BizRepairOrder order = repairOrderService.getById(orderId);
        if (order != null) {
            order.setWorkerId(workerId);
            order.setStatus(1); //强制变更为维修中
            repairOrderService.updateById(order);

            //通知师傅
            SysMessage msg = new SysMessage();
            msg.setReceiverId(workerId);
            msg.setTitle("管理员指派工单");
            msg.setContent("管理员为您指派了工单【" + order.getTitle() + "】，请及时处理。");
            msg.setType("REPAIR");
            msg.setIsRead(0);
            msg.setCreateTime(java.time.LocalDateTime.now());
            messageMapper.insert(msg);

            return Result.success(true);
        }
        return Result.error(400, "工单不存在");
    }
}