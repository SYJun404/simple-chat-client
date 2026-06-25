package com.chatapp.spring.res;

import java.time.LocalDateTime;

public class MesResponse {

    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private LocalDateTime sendTime;
    private Integer isRead;

    public MesResponse() {}

    public MesResponse(
        Long id,
        Long fromUserId,
        Long toUserId,
        String content,
        LocalDateTime sendTime,
        Integer isRead
    ) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
        this.sendTime = sendTime;
        this.isRead = isRead;
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

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }
}
