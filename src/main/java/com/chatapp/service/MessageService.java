package com.chatapp.service;

import com.chatapp.spring.api.ApiClient;
import com.chatapp.spring.res.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public class MessageService {

    private static MessageService instance;
    // key: "FromUserId|ToUserId", value: MesResonse
    private final Map<String, List<MesResponse>> chatHistory = new HashMap<>();

    public static MessageService getInstance() {
        if (instance == null) instance = new MessageService();
        return instance;
    }

    private String buildKey(Long a, Long b) {
        return a < b ? a + "|" + b : b + "|" + a;
    }

    public void sendMessage(Long fromId, Long toId, String content) {
        String key = buildKey(fromId, toId);
        chatHistory
            .computeIfAbsent(key, k -> new ArrayList<>())
            .add(
                new MesResponse(
                    0L,
                    fromId,
                    toId,
                    content,
                    LocalDateTime.now(),
                    0
                )
            );
    }

    public List<MesResponse> getHistory(Long a, Long b) {
        return chatHistory.getOrDefault(
            buildKey(a, b),
            Collections.emptyList()
        );
    }

    public void getHistoryFromServer(Long userId) {
        new SwingWorker<ApiResponse<List<MesResponse>>, Void>() {
            @Override
            protected ApiResponse<List<MesResponse>> doInBackground()
                throws Exception {
                return ApiClient.get(
                    "/chat/history?userId=" + userId,
                    new TypeReference<ApiResponse<List<MesResponse>>>() {}
                );
            }

            @Override
            protected void done() {
                try {
                    ApiResponse<List<MesResponse>> resp = get();
                    // 遍历输出结果
                    if (resp.getCode() == 200) {
                        List<MesResponse> messages = resp.getData();
                        for (MesResponse msg : messages) {
                            chatHistory
                                .computeIfAbsent(
                                    buildKey(
                                        msg.getFromUserId(),
                                        msg.getToUserId()
                                    ),
                                    k -> new ArrayList<>()
                                )
                                .add(msg);
                        }
                    }
                } catch (ExecutionException e) {
                    System.out.println(e);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }.execute();
    }
}
