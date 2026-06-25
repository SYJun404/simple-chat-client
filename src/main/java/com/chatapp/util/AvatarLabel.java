package com.chatapp.util;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.*;

public class AvatarLabel extends JPanel {

    private final Image avatar;
    private final int size;

    public AvatarLabel(Long userId, int size) {
        this.size = size;
        String imgPath = "src/main/resources/icons/" + (userId % 5) + ".png";
        this.avatar = new ImageIcon(imgPath)
            .getImage()
            .getScaledInstance(size, size, Image.SCALE_SMOOTH);
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));

        g2.setColor(Color.WHITE);
        g2.fillOval(0, 0, size, size);

        g2.drawImage(avatar, 0, 0, size, size, this);
        g2.dispose();
    }
}
