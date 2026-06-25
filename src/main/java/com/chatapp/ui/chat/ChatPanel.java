package com.chatapp.ui.chat;

import com.chatapp.service.MessageService;
import com.chatapp.spring.res.MesResponse;
import com.chatapp.spring.res.UserResponse;
import com.chatapp.util.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class ChatPanel extends JPanel {

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter YEAR_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MessageService messageService = MessageService.getInstance();

    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextArea inputArea;
    private JButton sendBtn;
    private String currentFriend;
    private Long fromUserId;
    private Long toUserId;

    public ChatPanel(Long curUserId) {
        this.fromUserId = curUserId;
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        initUI();
        showPlaceholder();
        messageService.getHistoryFromServer(curUserId);
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    private void initUI() {
        // 消息区域
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(UIConstants.BG_DARK);
        messagesPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        messagesPanel.setAutoscrolls(true);

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UIConstants.BG_DARK);
        scrollPane.getViewport().setBackground(UIConstants.BG_DARK);
        scrollPane.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 输入区域
        JPanel inputPanel = buildInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(UIConstants.BG_DARK);
        panel.setBorder(
            new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIConstants.BORDER),
                new EmptyBorder(12, 12, 12, 12)
            )
        );

        inputArea = new JTextArea(3, 0);
        inputArea.setFont(UIConstants.FONT_NORMAL);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBackground(UIConstants.BG_DARK);
        inputArea.setForeground(UIConstants.TEXT_PRIMARY);
        inputArea.setCaretColor(UIConstants.TEXT_PRIMARY);
        inputArea.setBorder(
            new CompoundBorder(
                new LineBorder(UIConstants.BORDER, 1, true),
                new EmptyBorder(8, 8, 8, 8)
            )
        );
        // Ctrl+Enter 发送
        inputArea.addKeyListener(
            new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (
                        e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()
                    ) {
                        e.consume();
                        doSend();
                    }
                }
            }
        );

        sendBtn = new StyledButton(
            "发 送",
            UIConstants.ACCENT,
            UIConstants.ACCENT_HOVER
        );
        sendBtn.setPreferredSize(new Dimension(80, 60));
        sendBtn.addActionListener(e -> doSend());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(inputArea, BorderLayout.CENTER);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(sendBtn, BorderLayout.EAST);
        return panel;
    }

    public void loadChat(String friendUsername) {
        this.currentFriend = friendUsername;
        inputArea.setEnabled(true);
        inputArea.setBackground(UIConstants.BG_PANEL);
        refreshMessages();
        inputArea.requestFocus();
    }

    private void refreshMessages() {
        messagesPanel.removeAll();

        Long me = fromUserId;
        if (me == null || toUserId == null) return;

        List<MesResponse> messages = messageService.getHistory(me, toUserId);

        if (messages.isEmpty()) {
            // 空状态居中
            messagesPanel.setLayout(
                new BoxLayout(messagesPanel, BoxLayout.Y_AXIS)
            );
            messagesPanel.add(Box.createVerticalGlue());
            JLabel empty = new JLabel("暂无消息，发个招呼吧 👋");
            empty.setForeground(UIConstants.TEXT_SECONDARY);
            empty.setFont(UIConstants.FONT_NORMAL);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            messagesPanel.add(empty);
            messagesPanel.add(Box.createVerticalGlue());
        } else {
            // 消息从底部往上堆叠（聊天软件标准行为）
            messagesPanel.setLayout(
                new BoxLayout(messagesPanel, BoxLayout.Y_AXIS)
            );
            messagesPanel.add(Box.createVerticalGlue());
            for (MesResponse msg : messages) {
                boolean isMine = msg.getFromUserId().equals(me);
                messagesPanel.add(buildBubble(msg, isMine));
                messagesPanel.add(Box.createVerticalStrut(8));
            }
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();
        scrollToBottom();
    }

    private JPanel buildBubble(MesResponse msg, boolean isMine) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        AvatarLabel avatar = new AvatarLabel(msg.getFromUserId(), 32);

        // 气泡内容
        JPanel bubble = new RoundedPanel(
            16,
            isMine ? UIConstants.ACCENT : UIConstants.BG_BUBBLE_OTHER
        );
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

        JTextArea content = new JTextArea(msg.getContent());
        content.setEditable(false);
        content.setOpaque(false);
        content.setFont(UIConstants.FONT_NORMAL.deriveFont(15f));
        content.setForeground(
            isMine ? UIConstants.TEXT_WHITE : UIConstants.TEXT_PRIMARY
        );
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        content.setMargin(new Insets(0, 0, 6, 0));

        JLabel time = new JLabel(formatSendTime(msg.getSendTime()));
        time.setFont(UIConstants.FONT_SMALL);
        time.setForeground(
            isMine ? new Color(0xBDD9F5) : UIConstants.TEXT_SECONDARY
        );

        bubble.add(content, BorderLayout.CENTER);
        bubble.add(time, BorderLayout.SOUTH);

        // 限制气泡宽度
        JPanel bubbleWrapper = new JPanel(
            new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0)
        );
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

        messageService.sendMessage(fromUserId, toUserId, text);
        inputArea.setText("");
        refreshMessages();
    }

    /** 格式化发送时间：今天只显示 HH:mm，否则显示 MM-dd HH:mm，跨年显示 yyyy-MM-dd */
    private String formatSendTime(java.time.LocalDateTime sendTime) {
        if (sendTime == null) return "";
        LocalDate today = LocalDate.now();
        LocalDate msgDate = sendTime.toLocalDate();
        if (msgDate.equals(today)) {
            return sendTime.format(TIME_FMT);
        } else if (msgDate.getYear() == today.getYear()) {
            return sendTime.format(DATE_FMT) + " " + sendTime.format(TIME_FMT);
        } else {
            return sendTime.format(YEAR_FMT) + " " + sendTime.format(TIME_FMT);
        }
    }

    private void showPlaceholder() {
        inputArea.setEnabled(false);

        messagesPanel.removeAll();
        JLabel lbl = new JLabel("Just chat. No distractions.");
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        lbl.setFont(UIConstants.FONT_LARGE);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        messagesPanel.add(Box.createVerticalGlue());
        messagesPanel.add(lbl);
        messagesPanel.add(Box.createVerticalGlue());
        messagesPanel.revalidate();
    }

    private void scrollToBottom() {
        EventQueue.invokeLater(() -> {
            // 找到最后添加的消息行组件，使其可见
            for (int i = messagesPanel.getComponentCount() - 1; i >= 0; i--) {
                Component c = messagesPanel.getComponent(i);
                // 跳过 glue（高度为 0），找到第一个有实际高度的组件
                if (c.isVisible() && c.getHeight() > 0) {
                    ((JComponent) c).scrollRectToVisible(c.getBounds());
                    break;
                }
            }
        });
    }
}
