package com.chatapp.ui.friend;

import com.chatapp.service.UserService;
import com.chatapp.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class AddFriendDialog extends JDialog {

    private final UserService userService = UserService.getInstance();
    private final Runnable onAdded;
    private JTextField searchField;
    private JLabel resultLabel;

    public AddFriendDialog(JFrame parent, Runnable onAdded) {
        super(parent, "添加好友", true);
        this.onAdded = onAdded;
        setSize(380, 280);
        setResizable(false);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIConstants.BG_PANEL);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 标题
        JLabel title = new JLabel("添加好友");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // 搜索区
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 36));
        searchField.putClientProperty("JTextField.placeholderText", "输入对方用户名");
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doAdd();
            }
        });

        StyledButton addBtn = new StyledButton("添加", UIConstants.ACCENT, UIConstants.ACCENT_HOVER);
        addBtn.setPreferredSize(new Dimension(70, 36));
        addBtn.addActionListener(e -> doAdd());

        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(addBtn, BorderLayout.EAST);

        // 结果提示
        resultLabel = new JLabel(" ");
        resultLabel.setFont(UIConstants.FONT_NORMAL);
        resultLabel.setForeground(UIConstants.TEXT_SECONDARY);
        resultLabel.setBorder(new EmptyBorder(14, 0, 0, 0));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 说明
        JLabel hint = new JLabel("输入好友的用户名即可添加");
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(UIConstants.TEXT_SECONDARY);
        hint.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(searchRow);
        center.add(hint);
        center.add(resultLabel);

        // 关闭按钮
        StyledButton closeBtn = new StyledButton("关 闭", new Color(0xC8CDD4), new Color(0xB0B8C1), UIConstants.TEXT_PRIMARY);
        closeBtn.setPreferredSize(new Dimension(0, 36));
        closeBtn.addActionListener(e -> dispose());

        root.add(title, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(closeBtn, BorderLayout.SOUTH);

        setContentPane(root);
        SwingUtilities.invokeLater(() -> searchField.requestFocus());
    }

    private void doAdd() {
        String target = searchField.getText().trim();
        if (target.isEmpty()) {
            setResult("请输入用户名", false);
            return;
        }

        String result = userService.addFriend(target);
        boolean success = result.startsWith("成功");
        setResult(success ? "✓ " + result : "✗ " + result, success);

        if (success) {
            onAdded.run();
            searchField.setText("");
        }
    }

    private void setResult(String msg, boolean success) {
        resultLabel.setText(msg);
        resultLabel.setForeground(success ? new Color(0x4CAF80) : new Color(0xE05B6F));
    }
}
