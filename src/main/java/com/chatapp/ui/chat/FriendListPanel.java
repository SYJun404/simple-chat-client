package com.chatapp.ui.chat;

import com.chatapp.spring.api.ApiClient;
import com.chatapp.spring.res.*;
import com.chatapp.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FriendListPanel extends JPanel {

    private final SessionManager session = SessionManager.getInstance();
    private final Consumer<String> onSelect;
    private JPanel listContainer;
    private String selectedUsername;

    public FriendListPanel(Consumer<String> onSelect) {
        this.onSelect = onSelect;
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SIDEBAR);

        // 标签
        JLabel label = new JLabel("  好友列表");
        label.setFont(UIConstants.FONT_SMALL);
        label.setForeground(UIConstants.TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(10, 8, 4, 0));
        add(label, BorderLayout.NORTH);

        // 列表
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(UIConstants.BG_SIDEBAR);

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.setBackground(UIConstants.BG_SIDEBAR);
        scroll.getViewport().setBackground(UIConstants.BG_SIDEBAR);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        new SwingWorker<ApiResponse<List<UserResponse>>, Void>() {
            @Override
            protected ApiResponse<List<UserResponse>> doInBackground()
                throws Exception {
                return ApiClient.get(
                    "/friends/get?userId=" + session.getId(),
                    new TypeReference<ApiResponse<List<UserResponse>>>() {}
                );
            }

            @Override
            protected void done() {
                try {
                    ApiResponse<List<UserResponse>> resp = get();

                    if (resp.getCode() == 200) {
                        session.setFriendList(resp.getData());
                        renderList();
                    }
                } catch (ExecutionException e) {
                    System.out.println(e);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }.execute();
    }

    public void renderList() {
        listContainer.removeAll();

        List<UserResponse> friendList = session.getFriendList();

        if (friendList == null) {
            JLabel empty = new JLabel("暂无好友，快去添加吧!");
            empty.setForeground(UIConstants.TEXT_SECONDARY);
            empty.setFont(UIConstants.FONT_SMALL);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setBorder(new EmptyBorder(30, 0, 0, 0));
            empty.setAlignmentX(CENTER_ALIGNMENT);
            listContainer.add(empty);
        }

        for (UserResponse friend : friendList) {
            if (friend != null) {
                listContainer.add(buildFriendItem(friend));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel buildFriendItem(UserResponse friend) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(UIConstants.BG_SIDEBAR);
        item.setBorder(new EmptyBorder(8, 12, 8, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        AvatarLabel iconAvatar = new AvatarLabel(
            friend.getId() % 5,
            UIConstants.AVATAR_SIZE
        );

        JPanel info = new JPanel(new GridLayout(2, 1, 0, -4));
        info.setOpaque(false);

        JLabel name = new JLabel(friend.getNickname());
        name.setFont(UIConstants.FONT_NORMAL);
        name.setForeground(UIConstants.TEXT_PRIMARY);

        String status = friend.getStatus() == 1 ? "● 在线" : "○ 离线";
        JLabel sub = new JLabel(status);
        sub.setFont(UIConstants.FONT_SMALL);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        if (friend.getStatus() == 1) {
            sub.setForeground(new Color(0x18A058));
        }

        info.add(name);
        info.add(sub);

        item.add(iconAvatar, BorderLayout.WEST);
        item.add(info, BorderLayout.CENTER);

        // 悬浮 + 选中效果
        item.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (
                        !friend.getUsername().equals(selectedUsername)
                    ) item.setBackground(new Color(0xE2E5EA));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (
                        !friend.getUsername().equals(selectedUsername)
                    ) item.setBackground(UIConstants.BG_SIDEBAR);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedUsername = friend.getUsername();
                    // 重置其他项
                    for (Component c : listContainer.getComponents()) {
                        c.setBackground(UIConstants.BG_SIDEBAR);
                    }
                    item.setBackground(new Color(0xD3E4F5));
                    onSelect.accept(friend.getUsername());
                }
            }
        );

        return item;
    }
}
