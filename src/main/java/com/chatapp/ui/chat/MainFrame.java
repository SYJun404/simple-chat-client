package com.chatapp.ui.chat;

import com.chatapp.spring.res.UserResponse;
import com.chatapp.ui.friend.AddFriendDialog;
import com.chatapp.ui.login.LoginFrame;
import com.chatapp.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class MainFrame extends JFrame {

    private FriendListPanel friendListPanel;
    private ChatPanel chatPanel;
    private JLabel chatTitleLabel;
    private SessionManager session = SessionManager.getInstance();

    public MainFrame() {
        setTitle("Simple Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG_DARK);

        // ── 左侧边栏 ──
        JPanel sidebar = buildSidebar();
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));

        // ── 右侧主区域 ──
        JPanel mainArea = buildMainArea();

        // 分割线
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setForeground(UIConstants.BORDER);
        sep.setPreferredSize(new Dimension(1, 0));

        root.add(sidebar, BorderLayout.WEST);
        root.add(sep, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            sidebar,
            mainArea
        );
        split.setDividerLocation(UIConstants.SIDEBAR_WIDTH);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(UIConstants.BORDER);

        setContentPane(split);
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
            UIConstants.AVATAR_SIZE
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
            new AddFriendDialog(this, () ->
                friendListPanel.refresh()
            ).setVisible(true);
        });

        StyledButton logoutBtn = new StyledButton(
            "× 退出",
            new Color(0xC8CDD4),
            new Color(0xB0B8C1),
            UIConstants.TEXT_PRIMARY
        );
        logoutBtn.addActionListener(e -> {
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
                new EmptyBorder(17, 20, 17, 20)
            )
        );

        chatTitleLabel = new JLabel("← 从左侧选择好友开始聊天");
        chatTitleLabel.setFont(UIConstants.FONT_LARGE);
        chatTitleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        titleBar.add(chatTitleLabel, BorderLayout.WEST);

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
    }
}
