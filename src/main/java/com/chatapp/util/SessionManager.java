package com.chatapp.util;

public class SessionManager {

    private static SessionManager instance;

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(
        Long id,
        String username,
        String nickname,
        String avatar,
        Integer status
    ) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.status = status;
    }

    public void logout() {
        id = null;
        username = null;
        nickname = null;
        avatar = null;
        status = null;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public boolean isLoggedIn() {
        return username != null;
    }
}
