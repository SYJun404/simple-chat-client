package com.chatapp.ui.login;

import com.chatapp.service.UserService;
import com.chatapp.ui.chat.MainFrame;
import com.chatapp.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private final UserService userService = UserService.getInstance();
    private JTabbedPane tabbedPane;

    // 登录组件
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // 注册组件
    private JTextField regUsernameField;
    private JTextField regNicknameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmField;

    public LoginFrame() {
        setTitle("ChatApp - 欢迎");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(420, 540);
        setLocationRelativeTo(null);
        setBackground(UIConstants.BG_DARK);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG_DARK);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // 顶部 Logo 区域
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel icon = new JLabel("💬");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title = new JLabel("ChatApp");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel("简单 · 高效 · 畅连");
        subtitle.setFont(UIConstants.FONT_SMALL);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        logoPanel.add(icon, BorderLayout.NORTH);
        logoPanel.add(title, BorderLayout.CENTER);
        logoPanel.add(subtitle, BorderLayout.SOUTH);

        // Tab 面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UIConstants.BG_PANEL);
        tabbedPane.setForeground(UIConstants.TEXT_PRIMARY);
        tabbedPane.setFont(UIConstants.FONT_NORMAL);
        tabbedPane.add("登 录", buildLoginPanel());
        tabbedPane.add("注 册", buildRegisterPanel());

        root.add(logoPanel, BorderLayout.NORTH);
        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.BG_PANEL);
        panel.setBorder(new EmptyBorder(20, 10, 20, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.gridx = 0; gbc.weightx = 1.0;

        loginUsernameField = createTextField("用户名");
        loginPasswordField = createPasswordField("密码");

        StyledButton loginBtn = new StyledButton("登 录", UIConstants.ACCENT, UIConstants.ACCENT_HOVER);
        loginBtn.setPreferredSize(new Dimension(0, 40));
        loginBtn.addActionListener(e -> doLogin());

        // Enter 触发登录
        loginPasswordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });

        JLabel hint = new JLabel("演示账号：admin / 123456");
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(UIConstants.TEXT_SECONDARY);
        hint.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0; panel.add(new JLabel("  用户名"), gbc);
        gbc.gridy = 1; panel.add(loginUsernameField, gbc);
        gbc.gridy = 2; panel.add(new JLabel("  密　码"), gbc);
        gbc.gridy = 3; panel.add(loginPasswordField, gbc);
        gbc.gridy = 4; gbc.insets = new Insets(16, 4, 6, 4);
        panel.add(loginBtn, gbc);
        gbc.gridy = 5; gbc.insets = new Insets(4, 4, 4, 4);
        panel.add(hint, gbc);

        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel) {
                ((JLabel) c).setForeground(UIConstants.TEXT_SECONDARY);
                ((JLabel) c).setFont(UIConstants.FONT_SMALL);
            }
        }
        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.BG_PANEL);
        panel.setBorder(new EmptyBorder(16, 10, 16, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0; gbc.weightx = 1.0;

        regUsernameField  = createTextField("登录用户名");
        regNicknameField  = createTextField("显示昵称");
        regPasswordField  = createPasswordField("设置密码");
        regConfirmField   = createPasswordField("确认密码");

        StyledButton regBtn = new StyledButton("注 册", UIConstants.ACCENT, UIConstants.ACCENT_HOVER);
        regBtn.setPreferredSize(new Dimension(0, 40));
        regBtn.addActionListener(e -> doRegister());

        Object[][] rows = {
            {"  用户名", regUsernameField},
            {"  昵　称", regNicknameField},
            {"  密　码", regPasswordField},
            {"  确认密码", regConfirmField}
        };

        int row = 0;
        for (Object[] r : rows) {
            JLabel label = new JLabel((String) r[0]);
            label.setForeground(UIConstants.TEXT_SECONDARY);
            label.setFont(UIConstants.FONT_SMALL);
            gbc.gridy = row++;
            panel.add(label, gbc);
            gbc.gridy = row++;
            panel.add((Component) r[1], gbc);
        }

        gbc.gridy = row; gbc.insets = new Insets(14, 4, 6, 4);
        panel.add(regBtn, gbc);
        return panel;
    }

    private void doLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("用户名和密码不能为空");
            return;
        }

        if (userService.login(username, password)) {
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        } else {
            showError("用户名或密码错误");
        }
    }

    private void doRegister() {
        String username  = regUsernameField.getText().trim();
        String nickname  = regNicknameField.getText().trim();
        String password  = new String(regPasswordField.getPassword());
        String confirm   = new String(regConfirmField.getPassword());

        if (username.isEmpty() || nickname.isEmpty() || password.isEmpty()) {
            showError("所有字段不能为空");
            return;
        }
        if (!password.equals(confirm)) {
            showError("两次密码不一致");
            return;
        }
        if (password.length() < 6) {
            showError("密码至少 6 位");
            return;
        }

        if (userService.register(username, password, nickname)) {
            JOptionPane.showMessageDialog(this, "注册成功！请登录", "提示", JOptionPane.INFORMATION_MESSAGE);
            tabbedPane.setSelectedIndex(0);
            loginUsernameField.setText(username);
        } else {
            showError("用户名已被使用");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 36));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(0, 36));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }
}
