package com.example.demo.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SysMessageEvent extends ApplicationEvent {
    private final Long receiverId;
    private final String title;
    private final String content;
    private final String type;

    public SysMessageEvent(Object source, Long receiverId, String title, String content, String type) {
        super(source); // source 是触发事件的源对象
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.type = type;
    }
}