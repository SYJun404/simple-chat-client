package com.chatapp.spring.res;

public class RecResponse {

    /** 当前记录的请求ID */
    private Long reqId;

    /** 发起请求的用户ID */
    private Long fromUserId;

    /** 发起请求的用户名 */
    private String fromUsername;

    /** 发起请求的用户昵称 */
    private String fromUserNickname;

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setReqId(Long reqId) {
        this.reqId = reqId;
    }

    public Long getReqId() {
        return reqId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getFromUserNickname() {
        return fromUserNickname;
    }

    public void setFromUserNickname(String fromUserNickname) {
        this.fromUserNickname = fromUserNickname;
    }
}
