package com.chatapp.util;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class AvatarLabel extends JPanel {

    private final Image avatar;
    private final int size;
    private final int type;

    public AvatarLabel(Long userId, int size, int type) {
        this.type = type;
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

        // 1. 在离屏缓冲上先画一个抗锯齿的圆形(作为alpha蒙版+背景色)
        BufferedImage buffer = new BufferedImage(
            size,
            size,
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D gBuf = buffer.createGraphics();
        gBuf.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        gBuf.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        );
        gBuf.setRenderingHint(
            RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_STROKE_PURE
        );

        // 背景色圆(抗锯齿边缘)
        gBuf.setColor(type == 0 ? Color.WHITE : UIConstants.ACCENT);
        gBuf.fill(new Ellipse2D.Float(0, 0, size, size));

        // 2. 用 DstIn 把头像图像"裁"进这个圆形alpha里,边缘会跟着圆的抗锯齿走
        gBuf.setComposite(AlphaComposite.SrcAtop); // 头像盖在背景色上,但仍保持圆形alpha
        gBuf.drawImage(avatar, 0, 0, size, size, this);
        gBuf.dispose();

        // 3. 把整张已经抗锯齿好的离屏图一次性画到组件上
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        g2.drawImage(buffer, 0, 0, this);
        g2.dispose();
    }
}
