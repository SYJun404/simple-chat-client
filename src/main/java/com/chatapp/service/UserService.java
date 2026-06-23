package com.chatapp.service;

import com.chatapp.model.User;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务（内存存储，实际项目可替换为数据库）
 */
public class UserService {

    private static UserService instance;
    private final Map<String, User> users = new HashMap<>();
    private User currentUser;

    private UserService() {
        // 预置测试账号
        User demo = new User("admin", "123456", "管理员");
        demo.addFriend("alice");
        demo.addFriend("bob");
        users.put("admin", demo);

        User alice = new User("alice", "123456", "Alice");
        alice.addFriend("admin");
        users.put("alice", alice);

        User bob = new User("bob", "123456", "Bob");
        bob.addFriend("admin");
        users.put("bob", bob);
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /** 登录，成功返回 true */
    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    /** 注册，用户名已存在返回 false */
    public boolean register(String username, String password, String nickname) {
        if (users.containsKey(username)) return false;
        if (username.isBlank() || password.isBlank()) return false;
        users.put(username, new User(username, password, nickname));
        return true;
    }

    /** 添加好友，返回结果消息 */
    public String addFriend(String friendUsername) {
        if (currentUser == null) return "未登录";
        if (
            friendUsername.equals(currentUser.getUsername())
        ) return "不能添加自己";
        if (!users.containsKey(friendUsername)) return (
            "用户 " + friendUsername + " 不存在"
        );
        if (
            currentUser.getFriends().contains(friendUsername)
        ) return "已是好友";
        currentUser.addFriend(friendUsername);
        // 双向添加
        users.get(friendUsername).addFriend(currentUser.getUsername());
        return "成功添加好友：" + users.get(friendUsername).getNickname();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getUserByUsername(String username) {
        return users.get(username);
    }

    public void logout() {
        currentUser = null;
    }
}
