package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.BizRepairOrder;

public interface BizRepairOrderService extends IService<BizRepairOrder> {
    //学生提交报修
    boolean submitOrder(BizRepairOrder order);
    //维修工接单
    boolean takeOrder(Long orderId, Long workerId);
    //维修工完成维修
    boolean finishRepair(Long orderId);
}