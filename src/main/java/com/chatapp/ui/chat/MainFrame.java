package com.chatapp.ui.chat;

import com.chatapp.service.MessageService;
import com.chatapp.spring.api.ApiClient;
import com.chatapp.spring.res.MesResponse;
import com.chatapp.spring.res.UserResponse;
import com.chatapp.ui.friend.AddFriendDialog;
import com.chatapp.ui.friend.VerifyFriendDialog;
import com.chatapp.ui.login.LoginFrame;
import com.chatapp.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

public class MainFrame extends JFrame {

    private FriendListPanel friendListPanel;
    private ChatPanel chatPanel;
    private JLabel chatTitleLabel;
    private SessionManager session = SessionManager.getInstance();
    private MessageService messageService = MessageService.getInstance();

    public MainFrame() {
        setTitle("Simple Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);
        initUI();

        // 是否有好友申请认证请求
        checkFriendReq();
        // 连接服务器
        try {
            messageService.connect(
                "localhost",
                8412,
                session.getUsername(),
                parts -> {
                    SwingUtilities.invokeLater(() -> handleProtocol(parts));
                }
            );
        } catch (IOException e) {
            showError("连接服务器失败：" + e.getMessage());
        }
    }

    private void checkFriendReq() {
        javax.swing.Timer timer = new javax.swing.Timer(1500, e -> {
            VerifyFriendDialog verifyFriendDialog = new VerifyFriendDialog(
                this
            );
            verifyFriendDialog.checkAndShow(session.getUsername());
        });
        timer.setRepeats(false);
        timer.start();
    }

    private static final ObjectMapper MAPPER =
        new ObjectMapper().registerModule(new JavaTimeModule());

    private void handleProtocol(String[] parts) {
        switch (parts[0]) {
            case "MSG":
                handleMsg(parts[1]);
                break;
            case "SERVER":
                handleServer(parts);
                break;
        }
    }

    private void handleMsg(String jsonStr) {
        try {
            MesResponse mes = MAPPER.readValue(jsonStr, MesResponse.class);

            // 存入本地缓存
            String key =
                mes.getFromUserId() < mes.getToUserId()
                    ? mes.getFromUserId() + "|" + mes.getToUserId()
                    : mes.getToUserId() + "|" + mes.getFromUserId();
            messageService
                .getChatHistory()
                .computeIfAbsent(key, k -> new java.util.ArrayList<>())
                .add(mes);
            AudioUtil.play("/audio/message.mp3");

            // 如果当前正在和该消息相关方聊天，刷新消息列表
            Long activeFriend = chatPanel.getToUserId();
            if (
                activeFriend.equals(mes.getFromUserId()) ||
                session.getId().equals(mes.getFromUserId())
            ) {
                chatPanel.refreshMessages();
            }
        } catch (Exception e) {
            System.out.println("消息解析失败: " + e.getMessage());
        }
    }

    private void handleServer(String[] parts) {
        String head = parts[1];
        if (
            head.equals("USER_LOGIN") && !parts[2].equals(session.getUsername())
        ) {
            handleUserLogin(parts[2]);
        } else if (head.equals("USER_LOGOUT")) {
            handleUserLogout();
        } else if (head.equals("FRIEND_REQUEST")) {
            handleFriendRequest();
        } else if (head.equals("FRIEND_ACCEPT")) {
            handleFriendAccept();
        }
    }

    private void handleUserLogin(String userName) {
        friendListPanel.refresh();
        if (session.getFriendByName(userName) != null) {
            AudioUtil.play("/audio/login.mp3");
        }
    }

    private void handleUserLogout() {
        javax.swing.Timer timer = new javax.swing.Timer(100, e ->
            friendListPanel.refresh()
        );
        timer.setRepeats(false);
        timer.start();
    }

    private void handleFriendRequest() {
        SwingUtilities.invokeLater(() -> {
            VerifyFriendDialog dialog = new VerifyFriendDialog(this);
            dialog.checkAndShow(session.getUsername());
        });
    }

    private void handleFriendAccept() {
        friendListPanel.refresh();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG_DARK);

        // ── 左侧边栏（固定宽度 + 右边框作为分割线）──
        JPanel sidebar = buildSidebar();
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, UIConstants.BORDER));

        // ── 右侧主区域 ──
        JPanel mainArea = buildMainArea();

        root.add(sidebar, BorderLayout.WEST);
        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.BG_SIDEBAR);

        // 顶部用户信息
        JPanel userBar = buildUserBar();
        panel.add(userBar, BorderLayout.NORTH);

        // 好友列表
        friendListPanel = new FriendListPanel(this::openChat);
        panel.add(friendListPanel, BorderLayout.CENTER);

        // 底部操作按钮
        JPanel btnBar = buildSidebarButtons();
        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildUserBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(UIConstants.BG_SIDEBAR);
        panel.setBorder(new EmptyBorder(6, 12, 6, 12));

        // 根据ID获取图片地址
        AvatarLabel iconAvatar = new AvatarLabel(
            session.getId(),
            UIConstants.AVATAR_SIZE,
            0
        );
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 0));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel(session.getNickname());
        nameLabel.setFont(UIConstants.FONT_NORMAL);
        nameLabel.setForeground(UIConstants.TEXT_PRIMARY);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        JLabel statusLabel = new JLabel("● 在线");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(new Color(0x18A058));

        info.add(nameLabel);
        info.add(statusLabel);

        panel.add(iconAvatar, BorderLayout.WEST);
        panel.add(info, BorderLayout.CENTER);

        // 底部分割线
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BG_SIDEBAR);
        wrapper.add(panel, BorderLayout.CENTER);
        JSeparator sep = new JSeparator();
        sep.setForeground(UIConstants.BORDER);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildSidebarButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 0));
        panel.setBackground(UIConstants.BG_SIDEBAR);
        panel.setBorder(new EmptyBorder(10, 12, 14, 12));

        StyledButton addFriendBtn = new StyledButton(
            "+ 好友",
            UIConstants.ACCENT,
            UIConstants.ACCENT_HOVER
        );
        addFriendBtn.addActionListener(e -> {
            new AddFriendDialog(this).setVisible(true);
        });

        StyledButton logoutBtn = new StyledButton(
            "× 退出",
            new Color(0xD4D4D4),
            new Color(0xB0B8C1),
            UIConstants.TEXT_PRIMARY
        );
        logoutBtn.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Map<String, String> body = Map.of(
                        "username",
                        session.getUsername()
                    );
                    // 只发送，不接收/解析返回值
                    ApiClient.post("/auth/logout", body, null);
                    return null;
                }
            }.execute();

            messageService.send("LOGOUT");
            session.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        panel.add(addFriendBtn);
        panel.add(logoutBtn);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BG_SIDEBAR);
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildMainArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.BG_DARK);

        // 顶部标题栏
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(UIConstants.BG_PANEL);
        titleBar.setBorder(
            new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                new EmptyBorder(10, 20, 10, 20)
            )
        );

        chatTitleLabel = new JLabel("← 从左侧选择好友开始聊天");
        chatTitleLabel.setFont(UIConstants.FONT_LARGE);
        chatTitleLabel.setForeground(UIConstants.TEXT_PRIMARY);

        AvatarLabel iconAvatar = new AvatarLabel(session.getId(), 32, 1);
        iconAvatar.setBackground(UIConstants.ACCENT);

        JLabel iconNameLabel = new JLabel(session.getNickname());
        iconNameLabel.setFont(UIConstants.FONT_LARGE);
        iconNameLabel.setForeground(UIConstants.ACCENT);
        iconNameLabel.setBorder(new EmptyBorder(0, 12, 0, 8));

        JPanel rightPanel = new RPanel();
        // 内层面板：label 和 avatar 紧贴排列，再用 CENTER 填充 RPanel
        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        innerPanel.setOpaque(false);
        innerPanel.add(iconNameLabel);
        innerPanel.add(iconAvatar);
        rightPanel.add(innerPanel, BorderLayout.CENTER);

        titleBar.add(chatTitleLabel, BorderLayout.WEST);
        titleBar.add(rightPanel, BorderLayout.EAST);

        // 聊天区域
        chatPanel = new ChatPanel(session.getId());

        panel.add(titleBar, BorderLayout.NORTH);
        panel.add(chatPanel, BorderLayout.CENTER);
        return panel;
    }

    private void openChat(String friendUsername) {
        UserResponse friend = session.getFriendByName(friendUsername);
        if (friend == null) return;
        chatTitleLabel.setText(friend.getNickname());
        chatPanel.setToUserId(friend.getId());
        chatPanel.loadChat(friend.getUsername());
        chatPanel.showInput();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(
            this,
            msg,
            "错误",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
