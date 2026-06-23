package com.chatapp.util;

import java.awt.*;

public class UIConstants {

    // 主色调
    public static final Color ACCENT = new Color(0x4a90d9);
    public static final Color ACCENT_HOVER = new Color(0x1A5FA8);

    // 背景
    public static final Color BG_DARK = new Color(0xF5F5F5);
    public static final Color BG_PANEL = new Color(0xFFFFFF);
    public static final Color BG_SIDEBAR = new Color(0xEEF0F3);
    public static final Color BG_BUBBLE_ME = new Color(0x2675BF);
    public static final Color BG_BUBBLE_OTHER = new Color(0xE4E7EB);

    // 文字
    public static final Color TEXT_PRIMARY = new Color(0x1E1E1E);
    public static final Color TEXT_SECONDARY = new Color(0x6B7280);
    public static final Color TEXT_WHITE = Color.WHITE;

    // 边框
    public static final Color BORDER = new Color(0xD1D5DB);

    // 字体（自动适配当前操作系统）
    public static final Font FONT_TITLE = FontProvider.getFont(Font.BOLD, 18);
    public static final Font FONT_LARGE = FontProvider.getFont(Font.PLAIN, 15);
    public static final Font FONT_NORMAL = FontProvider.getFont(Font.PLAIN, 13);
    public static final Font FONT_SMALL = FontProvider.getFont(Font.PLAIN, 11);

    // 尺寸
    public static final int SIDEBAR_WIDTH = 220;
    public static final int AVATAR_SIZE = 40;
    public static final int CORNER_RADIUS = 12;
}
