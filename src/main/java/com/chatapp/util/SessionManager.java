package com.chatapp.util;

import com.chatapp.spring.res.UserResponse;
import java.util.Comparator;
import java.util.List;

public class SessionManager {

    private static SessionManager instance;

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
    private List<UserResponse> friendList;

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

    public void setFriendList(List<UserResponse> friendList) {
        friendList.sort(
            Comparator.comparing(
                UserResponse::getStatus,
                Comparator.nullsLast(Integer::compareTo)
            ).reversed()
        );
        this.friendList = friendList;
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

    public List<UserResponse> getFriendList() {
        return this.friendList;
    }

    public UserResponse getFriendByName(String name) {
        if (name == null || friendList == null) return null;
        for (UserResponse friend : friendList) {
            if (name.equalsIgnoreCase(friend.getUsername())) {
                return friend;
            }
        }
        return null;
    }

    public boolean isLoggedIn() {
        return username != null;
    }
}
