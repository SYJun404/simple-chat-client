package com.chatapp.util;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * 字体提供者。
 * 优先从 resources 加载自定义字体文件（DingTalk-JinBuTi.ttf），
 * 注册到系统后使用；若加载失败则回退到系统预装中文字体。
 */
public class FontProvider {

    private static final String FONT_RESOURCE = "/DingTalk-JinBuTi.ttf";

    private static String availableFontName;

    static {
        // 1) 尝试从 resources 加载自定义字体
        try (
            InputStream is = FontProvider.class.getResourceAsStream(
                FONT_RESOURCE
            )
        ) {
            if (is != null) {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);

                // 注册成功后，用字体的家族名（family name）
                availableFontName = customFont.getFamily();
                System.out.println(
                    "[FontProvider] Loaded custom font: " + availableFontName
                );
            } else {
                System.err.println(
                    "[FontProvider] Font resource not found: " + FONT_RESOURCE
                );
            }
        } catch (FontFormatException | IOException e) {
            System.err.println(
                "[FontProvider] Failed to load custom font: " + e.getMessage()
            );
        }

        // 2) 如果自定义字体未加载成功，回退到系统预装字体
        if (availableFontName == null) {
            String[] cjkFallbacks = {
                "PingFang SC", // macOS 简体中文
                "PingFang TC", // macOS 繁体中文
                "Microsoft YaHei UI", // Windows 10+
                "微软雅黑", // Windows
                "Noto Sans CJK SC", // Linux
                "WenQuanYi Micro Hei", // Linux
                "STHeiti", // macOS
                "SimHei", // Windows / Linux
            };

            String[] systemFonts =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

            outer: for (String cjk : cjkFallbacks) {
                for (String name : systemFonts) {
                    if (
                        name.equalsIgnoreCase(cjk) ||
                        name.equalsIgnoreCase(cjk.replace(" ", ""))
                    ) {
                        availableFontName = name;
                        break outer;
                    }
                }
            }

            if (availableFontName == null) {
                availableFontName = "SansSerif";
            }
            System.out.println(
                "[FontProvider] Fallback to system font: " + availableFontName
            );
        }
    }

    public static Font getFont(int style, int size) {
        return new Font(availableFontName, style, size);
    }

    /** 获取当前实际使用的字体名 */
    public static String getResolvedFontName() {
        return availableFontName;
    }
}
