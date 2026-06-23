package com.chatapp.ui.chat;

import com.chatapp.model.Message;
import com.chatapp.service.MessageService;
import com.chatapp.service.UserService;
import com.chatapp.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ChatPanel extends JPanel {

    private final UserService userService = UserService.getInstance();
    private final MessageService messageService = MessageService.getInstance();

    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextArea inputArea;
    private JButton sendBtn;
    private String currentFriend;

    public ChatPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        initUI();
        showPlaceholder();
    }

    private void initUI() {
        // 消息区域
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(UIConstants.BG_DARK);
        messagesPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UIConstants.BG_DARK);
        scrollPane.getViewport().setBackground(UIConstants.BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 输入区域
        JPanel inputPanel = buildInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(UIConstants.BG_PANEL);
        panel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, UIConstants.BORDER),
            new EmptyBorder(12, 16, 12, 16)
        ));

        inputArea = new JTextArea(3, 0);
        inputArea.setFont(UIConstants.FONT_NORMAL);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBackground(UIConstants.BG_PANEL);
        inputArea.setForeground(UIConstants.TEXT_PRIMARY);
        inputArea.setCaretColor(UIConstants.TEXT_PRIMARY);
        inputArea.setBorder(new CompoundBorder(
            new LineBorder(UIConstants.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        // Ctrl+Enter 发送
        inputArea.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    e.consume();
                    doSend();
                }
            }
        });

        sendBtn = new StyledButton("发 送", UIConstants.ACCENT, UIConstants.ACCENT_HOVER);
        sendBtn.setPreferredSize(new Dimension(80, 60));
        sendBtn.addActionListener(e -> doSend());
        sendBtn.setEnabled(false);

        // 提示文字
        JLabel hint = new JLabel("Ctrl+Enter 发送");
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(UIConstants.TEXT_SECONDARY);
        hint.setBorder(new EmptyBorder(4, 2, 0, 0));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(inputArea, BorderLayout.CENTER);
        leftPanel.add(hint, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(sendBtn, BorderLayout.EAST);
        return panel;
    }

    public void loadChat(String friendUsername) {
        this.currentFriend = friendUsername;
        sendBtn.setEnabled(true);
        inputArea.setEnabled(true);
        refreshMessages();
        inputArea.requestFocus();
    }

    private void refreshMessages() {
        messagesPanel.removeAll();

        String me = userService.getCurrentUser().getUsername();
        List<Message> messages = messageService.getHistory(me, currentFriend);

        if (messages.isEmpty()) {
            JLabel empty = new JLabel("暂无消息，发个招呼吧 👋");
            empty.setForeground(UIConstants.TEXT_SECONDARY);
            empty.setFont(UIConstants.FONT_NORMAL);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            messagesPanel.add(Box.createVerticalGlue());
            messagesPanel.add(empty);
            messagesPanel.add(Box.createVerticalGlue());
        } else {
            messagesPanel.add(Box.createVerticalGlue());
            for (Message msg : messages) {
                boolean isMine = msg.getSender().equals(me);
                messagesPanel.add(buildBubble(msg, isMine));
                messagesPanel.add(Box.createVerticalStrut(8));
            }
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();
        scrollToBottom();
    }

    private JPanel buildBubble(Message msg, boolean isMine) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // 头像
        String senderNick = userService.getUserByUsername(msg.getSender()) != null
                ? userService.getUserByUsername(msg.getSender()).getNickname()
                : msg.getSender();
        AvatarPanel avatar = new AvatarPanel(senderNick, 34);

        // 气泡内容
        JPanel bubble = new RoundedPanel(16, isMine ? UIConstants.BG_BUBBLE_ME : UIConstants.BG_BUBBLE_OTHER);
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

        JTextArea content = new JTextArea(msg.getContent());
        content.setEditable(false);
        content.setOpaque(false);
        content.setFont(UIConstants.FONT_NORMAL);
        content.setForeground(isMine ? UIConstants.TEXT_WHITE : UIConstants.TEXT_PRIMARY);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        JLabel time = new JLabel(msg.getFormattedTime());
        time.setFont(UIConstants.FONT_SMALL);
        time.setForeground(isMine ? new Color(0xBDD9F5) : UIConstants.TEXT_SECONDARY);

        bubble.add(content, BorderLayout.CENTER);
        bubble.add(time, BorderLayout.SOUTH);

        // 限制气泡宽度
        JPanel bubbleWrapper = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        bubbleWrapper.setOpaque(false);
        bubbleWrapper.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        bubbleWrapper.add(bubble);

        if (isMine) {
            row.add(bubbleWrapper, BorderLayout.CENTER);
            row.add(avatar, BorderLayout.EAST);
            row.setBorder(new EmptyBorder(0, 60, 0, 0));
        } else {
            row.add(avatar, BorderLayout.WEST);
            row.add(bubbleWrapper, BorderLayout.CENTER);
            row.setBorder(new EmptyBorder(0, 0, 0, 60));
        }

        return row;
    }

    private void doSend() {
        if (currentFriend == null) return;
        String text = inputArea.getText().trim();
        if (text.isEmpty()) return;

        String me = userService.getCurrentUser().getUsername();
        messageService.sendMessage(me, currentFriend, text);
        inputArea.setText("");
        refreshMessages();
    }

    private void showPlaceholder() {
        sendBtn.setEnabled(false);
        inputArea.setEnabled(false);

        messagesPanel.removeAll();
        JLabel lbl = new JLabel("← 从左侧选择好友开始聊天");
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        lbl.setFont(UIConstants.FONT_LARGE);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        messagesPanel.add(Box.createVerticalGlue());
        messagesPanel.add(lbl);
        messagesPanel.add(Box.createVerticalGlue());
        messagesPanel.revalidate();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}
