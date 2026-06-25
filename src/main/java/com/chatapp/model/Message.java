package com.chatapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private LocalDateTime sendTime;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm");

    public Message(Long fromUserId, Long toUserId, String content) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
        this.sendTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public String getFormattedTime() {
        return sendTime != null ? sendTime.format(FORMATTER) : "";
    }
}
