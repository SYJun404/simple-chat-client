package com.chatapp.util;

import javax.swing.*;
import java.awt.*;

public class AvatarPanel extends JPanel {
    private String initial;
    private Color color;
    private int size;

    // 头像颜色池
    private static final Color[] COLORS = {
        new Color(0x5B8DEF), new Color(0xE05B6F), new Color(0x4CAF80),
        new Color(0xFF8C42), new Color(0x9B59B6), new Color(0x1ABC9C)
    };

    public AvatarPanel(String nickname, int size) {
        this.size = size;
        this.initial = nickname == null || nickname.isEmpty() ? "?" :
                       String.valueOf(nickname.charAt(0)).toUpperCase();
        // 根据名字哈希选色
        this.color = COLORS[Math.abs(nickname.hashCode()) % COLORS.length];
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(0, 0, size, size);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("微软雅黑", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(initial)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initial, x, y);
        g2.dispose();
    }
}
