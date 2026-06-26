package com.chatapp.ui.chat;

import com.chatapp.service.MessageService;
import com.chatapp.spring.res.MesResponse;
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
    private boolean isShowInput;
    private JPanel inputJ;

    public ChatPanel(Long curUserId) {
        this.fromUserId = curUserId;
        this.isShowInput = false;
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        initUI();
        showPlaceholder();
        messageService.getHistoryFromServer(curUserId);
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    private void initUI() {
        // 消息区域
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(UIConstants.BG_SIDEBAR);
        messagesPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        messagesPanel.setAutoscrolls(true);

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UIConstants.BG_DARK);
        scrollPane.getViewport().setBackground(UIConstants.BG_SIDEBAR);
        scrollPane.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        inputJ = buildInputPanel();
    }

    public void showInput() {
        // 输入区域
        if (!isShowInput) {
            add(inputJ, BorderLayout.SOUTH);
            this.isShowInput = true;
        }
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(UIConstants.BG_SIDEBAR);
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
        // Cmd+Enter 发送
        inputArea.addKeyListener(
            new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isMetaDown()) {
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

    public void refreshMessages() {
        messagesPanel.removeAll();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));

        Long me = fromUserId;
        if (me == null || toUserId == null) return;

        List<MesResponse> messages = messageService.getHistory(me, toUserId);

        if (messages.isEmpty()) {
            messagesPanel.add(Box.createVerticalGlue());
            JLabel empty = new JLabel("暂无消息，打个招呼吧 👋");
            empty.setForeground(UIConstants.TEXT_SECONDARY);
            empty.setFont(UIConstants.FONT_NORMAL);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            messagesPanel.add(empty);
            messagesPanel.add(Box.createVerticalGlue());
        } else {
            for (MesResponse msg : messages) {
                boolean isMine = msg.getFromUserId().equals(me);
                JPanel bubble = buildBubble(msg, isMine);

                bubble.setMaximumSize(
                    new Dimension(
                        Integer.MAX_VALUE,
                        bubble.getPreferredSize().height
                    )
                );
                messagesPanel.add(bubble);
                messagesPanel.add(Box.createVerticalStrut(8));
            }
            messagesPanel.add(Box.createVerticalGlue());
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();
        scrollToBottom();
    }

    private String normalizeForMeasure(String text) {
        // 匹配所有 emoji（包括肤色修饰符、ZWJ序列、变体选择符等）
        String emojiRegex =
            "[\\x{1F000}-\\x{1FFFF}\\x{2600}-\\x{27FF}\\x{FE00}-\\x{FEFF}\\x{1F3FB}-\\x{1F3FF}\\x{200D}]+";
        return text.replaceAll(emojiRegex, "□□");
    }

    private JPanel buildBubble(MesResponse msg, boolean isMine) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        AvatarLabel avatar = new AvatarLabel(msg.getFromUserId(), 32);

        // ★ 用 FontMetrics 量文字，决定气泡宽度
        Font msgFont = UIConstants.FONT_NORMAL.deriveFont(15f);
        FontMetrics fm = getFontMetrics(msgFont);
        String text = msg.getContent();

        int maxWidth = 320; // 气泡最大文字区宽度
        int hPad = 25; // 左右 padding 各 12 + 1
        int vPad = 8 + 6 + 16 + 8; // 上8 + content底margin6 + time行16 + 下8

        // 按 maxWidth 折行，计算实际需要几行
        String[] lines = normalizeForMeasure(msg.getContent()).split("\n", -1);

        int actualWidth = 0;
        int totalHeight = 0;
        for (String line : lines) {
            // 当前行超过 maxWidth 则折行
            int lineW = fm.stringWidth(line);
            if (lineW <= maxWidth) {
                actualWidth = Math.max(actualWidth, lineW);
                totalHeight += fm.getHeight();
            } else {
                // 超长行按 maxWidth 折算行数
                int rows = (int) Math.ceil((double) lineW / maxWidth);
                actualWidth = maxWidth;
                totalHeight += fm.getHeight() * rows;
            }
        }

        FontMetrics timeFm = getFontMetrics(UIConstants.FONT_SMALL);
        int timeWidth =
            timeFm.stringWidth(formatSendTime(msg.getSendTime())) + hPad;

        int bubbleW = Math.max(
            Math.min(actualWidth, maxWidth) + hPad,
            timeWidth
        );
        int bubbleH = totalHeight + vPad;

        // 气泡
        JPanel bubble = new RoundedPanel(
            16,
            isMine ? UIConstants.ACCENT : UIConstants.BG_BUBBLE_OTHER
        );
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));
        bubble.setPreferredSize(new Dimension(bubbleW, bubbleH));
        bubble.setMinimumSize(new Dimension(bubbleW, bubbleH));
        bubble.setMaximumSize(new Dimension(bubbleW, bubbleH));

        JTextArea content = new JTextArea(text);
        content.setEditable(false);
        content.setOpaque(false);
        content.setFont(msgFont);
        content.setForeground(
            isMine ? UIConstants.TEXT_WHITE : UIConstants.TEXT_PRIMARY
        );
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setMargin(new Insets(0, 0, 6, 0));

        JLabel time = new JLabel(formatSendTime(msg.getSendTime()));
        time.setFont(UIConstants.FONT_SMALL);
        time.setForeground(
            isMine ? new Color(0xBDD9F5) : UIConstants.TEXT_SECONDARY
        );

        bubble.add(content, BorderLayout.CENTER);
        bubble.add(time, BorderLayout.SOUTH);

        // 气泡靠左或靠右
        JPanel bubbleWrapper = new JPanel(
            new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0)
        );
        bubbleWrapper.setOpaque(false);
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
        lbl.setBorder(new EmptyBorder(0, 0, 60, 0));

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
