package com.example.demo.listener;

import com.example.demo.entity.SysMessage;
import com.example.demo.event.SysMessageEvent;
import com.example.demo.service.SysMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SysMessageListener {

    @Autowired
    private SysMessageService messageService;

    //@Async注解让这个方法在一个全新的独立线程中执行，不卡主业务
    @Async
    @EventListener
    public void handleMessageEvent(SysMessageEvent event) {
        System.out.println("【异步监听器触发】准备给用户 ID=" + event.getReceiverId() + " 发送系统通知...");
        
        SysMessage message = new SysMessage();
        message.setReceiverId(event.getReceiverId());
        message.setTitle(event.getTitle());
        message.setContent(event.getContent());
        message.setType(event.getType());
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        
        messageService.save(message);
        System.out.println("【异步监听器执行完毕】站内信成功落地");
    }
}