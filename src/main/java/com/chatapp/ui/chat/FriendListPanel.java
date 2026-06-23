package com.chatapp.ui.chat;

import com.chatapp.model.User;
import com.chatapp.service.UserService;
import com.chatapp.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class FriendListPanel extends JPanel {

    private final UserService userService = UserService.getInstance();
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
        label.setBorder(new EmptyBorder(10, 6, 4, 0));
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
        listContainer.removeAll();
        User me = userService.getCurrentUser();
        if (me == null) return;

        for (String friendUsername : me.getFriends()) {
            User friend = userService.getUserByUsername(friendUsername);
            if (friend != null) {
                listContainer.add(buildFriendItem(friend));
            }
        }

        if (me.getFriends().isEmpty()) {
            JLabel empty = new JLabel("暂无好友，快去添加吧");
            empty.setForeground(UIConstants.TEXT_SECONDARY);
            empty.setFont(UIConstants.FONT_SMALL);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setBorder(new EmptyBorder(30, 0, 0, 0));
            empty.setAlignmentX(CENTER_ALIGNMENT);
            listContainer.add(empty);
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel buildFriendItem(User friend) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(UIConstants.BG_SIDEBAR);
        item.setBorder(new EmptyBorder(8, 12, 8, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        AvatarPanel avatar = new AvatarPanel(friend.getNickname(), UIConstants.AVATAR_SIZE);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel name = new JLabel(friend.getNickname());
        name.setFont(UIConstants.FONT_NORMAL);
        name.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel sub = new JLabel(friend.getUsername());
        sub.setFont(UIConstants.FONT_SMALL);
        sub.setForeground(UIConstants.TEXT_SECONDARY);

        info.add(name);
        info.add(sub);

        item.add(avatar, BorderLayout.WEST);
        item.add(info, BorderLayout.CENTER);

        // 悬浮 + 选中效果
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!friend.getUsername().equals(selectedUsername))
                    item.setBackground(new Color(0xE2E5EA));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!friend.getUsername().equals(selectedUsername))
                    item.setBackground(UIConstants.BG_SIDEBAR);
            }
            @Override public void mouseClicked(MouseEvent e) {
                selectedUsername = friend.getUsername();
                // 重置其他项
                for (Component c : listContainer.getComponents()) {
                    c.setBackground(UIConstants.BG_SIDEBAR);
                }
                item.setBackground(new Color(0xD3E4F5));
                onSelect.accept(friend.getUsername());
            }
        });

        return item;
    }
}
