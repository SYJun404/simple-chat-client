package com.chatapp.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StyledButton extends JButton {
    private Color normalColor;
    private Color hoverColor;
    private int radius;

    public StyledButton(String text, Color normalColor, Color hoverColor) {
        this(text, normalColor, hoverColor, UIConstants.TEXT_WHITE);
    }

    public StyledButton(String text, Color normalColor, Color hoverColor, Color fgColor) {
        super(text);
        this.normalColor = normalColor;
        this.hoverColor  = hoverColor;
        this.radius = 10;

        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setForeground(fgColor);
        setFont(UIConstants.FONT_NORMAL);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited(MouseEvent e)  { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean hover = getModel().isRollover();
        g2.setColor(hover ? hoverColor : normalColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);

        g2.dispose();
        super.paintComponent(g);
    }
}
