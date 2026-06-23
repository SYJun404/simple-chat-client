package com.chatapp.service;

import com.chatapp.model.Message;
import java.util.*;

public class MessageService {

    private static MessageService instance;
    // key: "userA|userB" (按字母序), value: 消息列表
    private final Map<String, List<Message>> chatHistory = new HashMap<>();

    public static MessageService getInstance() {
        if (instance == null) instance = new MessageService();
        return instance;
    }

    private String buildKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    public void sendMessage(String from, String to, String content) {
        String key = buildKey(from, to);
        chatHistory.computeIfAbsent(key, k -> new ArrayList<>())
                   .add(new Message(from, to, content));
    }

    public List<Message> getHistory(String a, String b) {
        return chatHistory.getOrDefault(buildKey(a, b), Collections.emptyList());
    }
}
