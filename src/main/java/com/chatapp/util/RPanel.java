package com.chatapp.util;

import java.awt.*;
import javax.swing.JPanel;

public class RPanel extends JPanel {

    private Color bgColor = new Color(0xe9f1fb);
    private Color borderColor = UIConstants.ACCENT;
    private int borderThickness = 1;
    private int fixedHeight = 32; // -1表示不固定,跟随内容

    public RPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(8, 0)); // 8px是name和图片之间的间距,可调
    }

    public void setBgColor(Color c) {
        this.bgColor = c;
        repaint();
    }

    public void setBorderColor(Color c) {
        this.borderColor = c;
        repaint();
    }

    /** 固定面板高度,宽度仍由内容自适应 */
    public void setFixedHeight(int height) {
        this.fixedHeight = height;
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (fixedHeight > 0) {
            return new Dimension(d.width, fixedHeight);
        }
        return d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int w = getWidth();
        int h = getHeight();
        int radius = h; // 高度固定,用高度做圆角半径,保证左右是半圆胶囊形

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w, h, radius, radius);

        g2.setStroke(new BasicStroke(borderThickness));
        g2.setColor(borderColor);
        g2.drawRoundRect(
            borderThickness / 2,
            borderThickness / 2,
            w - borderThickness,
            h - borderThickness,
            radius,
            radius
        );

        g2.dispose();
    }
}
