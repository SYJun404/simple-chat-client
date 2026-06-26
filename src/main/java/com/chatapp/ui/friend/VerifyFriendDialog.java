package com.chatapp.ui.friend;

import com.chatapp.spring.api.ApiClient;
import com.chatapp.spring.res.*;
import com.chatapp.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.*;

/**
 * 好友验证对话框 —— 查看待处理的好友请求，接受或拒绝。
 */
public class VerifyFriendDialog extends JDialog {

    private JPanel listPanel;
    private JLabel title;
    private JPanel emptyHint;

    public VerifyFriendDialog(JFrame parent) {
        super(parent, "", true);
        setSize(400, 360);
        setResizable(false);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIConstants.BG_PANEL);
        root.setBorder(new EmptyBorder(24, 24, 20, 24));

        // ── 标题行 ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        title = new JLabel("好友验证");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        root.add(header, BorderLayout.NORTH);

        // ── 请求列表容器 ──
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        // 空状态提示
        emptyHint = new JPanel(new GridBagLayout());
        emptyHint.setOpaque(false);
        JLabel emptyLabel = new JLabel("暂未收到好友请求");
        emptyLabel.setFont(UIConstants.FONT_NORMAL);
        emptyLabel.setForeground(UIConstants.TEXT_SECONDARY);
        emptyHint.add(emptyLabel);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        root.add(scrollPane, BorderLayout.CENTER);

        // ── 底部关闭按钮 ──
        StyledButton closeBtn = new StyledButton(
            "关 闭",
            new Color(0xC8CDD4),
            new Color(0xB0B8C1),
            UIConstants.TEXT_PRIMARY
        );
        closeBtn.setPreferredSize(new Dimension(0, 36));
        closeBtn.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 0, 0, 0));
        footer.add(closeBtn, BorderLayout.CENTER);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    /**
     * 检查是否有待处理的请求，有则显示窗口，无则关闭。
     */
    public void checkAndShow(String username) {
        listPanel.removeAll();
        listPanel.add(Box.createVerticalStrut(60));
        // 用 JLabel 做 loading 提示
        JLabel loading = new JLabel("加载中...");
        loading.setFont(UIConstants.FONT_NORMAL);
        loading.setForeground(UIConstants.TEXT_SECONDARY);
        loading.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(loading);
        listPanel.revalidate();
        listPanel.repaint();

        new SwingWorker<ApiResponse<List<RecResponse>>, Void>() {
            @Override
            protected ApiResponse<List<RecResponse>> doInBackground()
                throws Exception {
                return ApiClient.get(
                    "/friends/pending-requests?userId=" + username,
                    new TypeReference<ApiResponse<List<RecResponse>>>() {}
                );
            }

            @Override
            protected void done() {
                try {
                    ApiResponse<List<RecResponse>> resp = get();
                    List<RecResponse> list = resp.getData();
                    if (list != null && !list.isEmpty()) {
                        AudioUtil.play("/audio/newfri.mp3");
                        showRequests(list);
                        setVisible(true);
                    } else {
                        dispose();
                    }
                } catch (ExecutionException e) {
                    dispose();
                    System.out.println(e);
                } catch (Exception e) {
                    dispose();
                    System.out.println(e);
                }
            }
        }.execute();
    }

    private void showRequests(List<RecResponse> requests) {
        listPanel.removeAll();
        listPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        for (RecResponse req : requests) {
            JPanel row = createRequestRow(req);
            listPanel.add(row);
            listPanel.add(Box.createVerticalStrut(8));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private void showEmpty() {
        listPanel.removeAll();
        listPanel.setLayout(new GridBagLayout());
        listPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel emptyLabel = new JLabel("暂未收到好友请求");
        emptyLabel.setFont(UIConstants.FONT_NORMAL);
        emptyLabel.setForeground(UIConstants.TEXT_SECONDARY);
        listPanel.add(emptyLabel);

        listPanel.revalidate();
        listPanel.repaint();
    }

    // ── 单行请求卡片 ──
    private JPanel createRequestRow(RecResponse requester) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(new Color(0xF9FAFB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0xE5E7EB));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setPreferredSize(new Dimension(0, 58));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 58));

        // 左侧：头像占位 + 昵称
        //
        AvatarLabel iconAvatar = new AvatarLabel(
            requester.getFromUserId() % 5,
            UIConstants.AVATAR_SIZE
        );

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel(requester.getFromUserNickname());
        nameLabel.setBorder(new EmptyBorder(3, 0, 0, 0));
        nameLabel.setFont(UIConstants.FONT_NORMAL.deriveFont(Font.BOLD, 13f));
        nameLabel.setForeground(UIConstants.TEXT_PRIMARY);
        info.add(nameLabel);

        String subText = "@" + requester.getFromUsername();
        JLabel subLabel = new JLabel(subText);
        subLabel.setFont(UIConstants.FONT_SMALL);
        subLabel.setForeground(UIConstants.TEXT_SECONDARY);
        info.add(subLabel);

        JPanel left = new JPanel(new BorderLayout(8, 0));
        left.setOpaque(false);
        left.add(iconAvatar, BorderLayout.WEST);
        left.add(info, BorderLayout.CENTER);

        // 右侧：接受 / 拒绝
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setBorder(new EmptyBorder(3, 0, 0, 0));
        actions.setOpaque(false);

        StyledButton acceptBtn = new StyledButton(
            "接受",
            UIConstants.ACCENT,
            UIConstants.ACCENT_HOVER
        );
        acceptBtn.setPreferredSize(new Dimension(60, 28));
        acceptBtn.setFont(UIConstants.FONT_SMALL);
        acceptBtn.addActionListener(e -> doAccept(requester, card));

        StyledButton rejectBtn = new StyledButton(
            "拒绝",
            new Color(0xE5E7EB),
            new Color(0xD1D5DB),
            UIConstants.TEXT_PRIMARY
        );
        rejectBtn.setPreferredSize(new Dimension(60, 28));
        rejectBtn.setFont(UIConstants.FONT_SMALL);
        rejectBtn.addActionListener(e -> doReject(requester, card));

        actions.add(acceptBtn);
        actions.add(rejectBtn);

        card.add(left, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);

        return card;
    }

    // ── 接受请求 ──
    private void doAccept(RecResponse requester, JPanel card) {
        Long curUserId = SessionManager.getInstance().getId();

        new SwingWorker<ApiResponse<Void>, Void>() {
            @Override
            protected ApiResponse<Void> doInBackground() throws Exception {
                Map<String, Long> body = new HashMap<>();
                body.put("fromUserId", requester.getFromUserId());
                body.put("toUserId", curUserId);
                body.put("recordId", requester.getReqId());
                return ApiClient.post(
                    "/friends/accepted-swing",
                    body,
                    new TypeReference<ApiResponse<Void>>() {}
                );
            }

            @Override
            protected void done() {
                try {
                    ApiResponse<Void> resp = get();
                    if (resp.getCode() == 200) {
                        markHandled(card, "已接受", new Color(0x10B981));
                    } else {
                        markHandled(
                            card,
                            resp.getMessage(),
                            new Color(0xEF4444)
                        );
                    }
                } catch (ExecutionException e) {
                    markHandled(card, "操作失败", new Color(0xEF4444));
                    System.out.println(e);
                } catch (Exception e) {
                    markHandled(card, "操作失败", new Color(0xEF4444));
                    System.out.println(e);
                }
            }
        }.execute();
    }

    // ── 拒绝请求 ──
    private void doReject(RecResponse requester, JPanel card) {
        new SwingWorker<ApiResponse<Void>, Void>() {
            @Override
            protected ApiResponse<Void> doInBackground() throws Exception {
                return ApiClient.post(
                    "/friends/requests/mark-read?recordId=" +
                        requester.getReqId(),
                    null,
                    new TypeReference<ApiResponse<Void>>() {}
                );
            }

            @Override
            protected void done() {
                try {
                    ApiResponse<Void> resp = get();
                    if (resp.getCode() == 200) {
                        markHandled(card, "已拒绝", new Color(0x6B7280));
                    } else {
                        markHandled(
                            card,
                            resp.getMessage(),
                            new Color(0xEF4444)
                        );
                    }
                } catch (ExecutionException e) {
                    markHandled(card, "操作失败", new Color(0xEF4444));
                    System.out.println(e);
                } catch (Exception e) {
                    markHandled(card, "操作失败", new Color(0xEF4444));
                    System.out.println(e);
                }
            }
        }.execute();
    }

    // ── 标记为已处理 ──
    private void markHandled(JPanel card, String text, Color color) {
        // 禁用按钮并显示状态文字
        for (Component comp : ((JPanel) card).getComponents()) {
            if (comp instanceof JPanel) {
                for (Component sub : ((JPanel) comp).getComponents()) {
                    if (sub instanceof StyledButton) {
                        sub.setEnabled(false);
                    }
                }
            }
        }

        JLabel statusLabel = new JLabel(text);
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(color);
        statusLabel.setBorder(new EmptyBorder(8, 0, 0, 4));

        JPanel east = (JPanel) ((JPanel) card).getComponent(1);
        east.removeAll();
        east.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        east.add(statusLabel);
        east.revalidate();
        east.repaint();
    }
}
