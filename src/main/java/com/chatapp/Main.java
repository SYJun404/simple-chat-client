package com.chatapp;

import com.chatapp.ui.login.LoginFrame;
import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();

        // 全局字体抗锯齿
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
