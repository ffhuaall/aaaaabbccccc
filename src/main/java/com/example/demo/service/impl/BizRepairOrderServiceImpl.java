package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.BizRepairOrder;
import com.example.demo.event.SysMessageEvent;
import com.example.demo.mapper.BizRepairOrderMapper;
import com.example.demo.service.BizRepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class BizRepairOrderServiceImpl extends ServiceImpl<BizRepairOrderMapper, BizRepairOrder> implements BizRepairOrderService {

    //注入事件发布器
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public boolean submitOrder(BizRepairOrder order) {
        order.setStatus(0);
        if (order.getCreateTime() == null) {
            order.setCreateTime(LocalDateTime.now());
        }
        return this.save(order);
    }

    @Override
    public boolean takeOrder(Long orderId, Long workerId) {
        BizRepairOrder order = this.getById(orderId);
        if (order != null && order.getStatus() == 0) {
            order.setStatus(1); 
            order.setWorkerId(workerId);
            boolean success = this.updateById(order);
            
            //业务流转成功后，异步发布消息事件通知发起报修的学生
            if (success) {
                String content = "您的报修工单【" + order.getTitle() + "】已被受理，后勤小哥正火速赶往现场！";
                eventPublisher.publishEvent(new SysMessageEvent(this, order.getStudentId(), "报修已受理", content, "REPAIR"));
            }
            return success;
        }
        return false;
    }

    @Override
    public boolean finishRepair(Long orderId) {
        BizRepairOrder order = this.getById(orderId);
        if (order != null && order.getStatus() == 1) {
            order.setStatus(2); 
            boolean success = this.updateById(order);
            
            //业务流转成功后，异步发布消息事件邀请学生评价
            if (success) {
                String content = "您的报修工单【" + order.getTitle() + "】已维修完毕。请前往门户确认验收并为小哥打个五星好评吧！";
                eventPublisher.publishEvent(new SysMessageEvent(this, order.getStudentId(), "报修待评价", content, "REPAIR"));
            }
            return success;
        }
        return false;
    }
}