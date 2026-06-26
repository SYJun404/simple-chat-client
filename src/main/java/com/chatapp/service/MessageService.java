package com.chatapp.service;

import com.chatapp.spring.api.ApiClient;
import com.chatapp.spring.res.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;

public class MessageService {

    private Socket socket;
    private PrintWriter out;
    private Consumer<String[]> onMessage; // 解析后的协议数组回调

    private static MessageService instance;
    // key: "FromUserId|ToUserId", value: MesResonse
    private final Map<String, List<MesResponse>> chatHistory = new HashMap<>();

    public static MessageService getInstance() {
        if (instance == null) instance = new MessageService();
        return instance;
    }

    public void connect(
        String host,
        int port,
        String userId,
        Consumer<String[]> callback
    ) throws IOException {
        this.onMessage = callback;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);

        new Thread(() -> {
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                )
            ) {
                String line;
                while ((line = in.readLine()) != null) {
                    onMessage.accept(line.split("\\|", -1));
                }
            } catch (IOException e) {
                onMessage.accept(new String[] { "SERVER", "DISCONNECTED" });
            }
        }).start();

        send("LOGIN", userId);
    }

    public void send(String... parts) {
        out.println(String.join("|", parts));
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

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Map<String, Object> body = Map.of(
                    "fromUserId",
                    fromId,
                    "toUserId",
                    toId,
                    "content",
                    content
                );
                // 只发送，不接收/解析返回值
                ApiClient.post("/chat/send-swing", body, null);
                return null;
            }
        }.execute();
    }

    public Map<String, List<MesResponse>> getChatHistory() {
        return chatHistory;
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
