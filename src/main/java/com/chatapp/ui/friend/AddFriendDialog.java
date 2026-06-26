package com.chatapp.ui.friend;

import com.chatapp.spring.api.ApiClient;
import com.chatapp.spring.res.*;
import com.chatapp.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.*;

public class AddFriendDialog extends JDialog {

    private JTextField searchField;
    private JLabel title;

    public AddFriendDialog(JFrame parent) {
        super(parent, "", true);
        setSize(380, 220);
        setResizable(false);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIConstants.BG_PANEL);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 标题
        title = new JLabel("添加好友");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // 搜索区
        JPanel searchRow = new JPanel();
        searchRow.setLayout(new BoxLayout(searchRow, BoxLayout.X_AXIS));
        searchRow.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 28));
        searchField.setMaximumSize(new Dimension(250, 28));
        searchField.putClientProperty(
            "JTextField.placeholderText",
            "输入对方用户名"
        );
        searchField.addKeyListener(
            new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) doAdd();
                }
            }
        );

        StyledButton addBtn = new StyledButton(
            "添加",
            UIConstants.ACCENT,
            UIConstants.ACCENT_HOVER
        );
        addBtn.setPreferredSize(new Dimension(70, 28));
        addBtn.addActionListener(e -> doAdd());

        searchRow.add(searchField);
        searchRow.add(Box.createHorizontalStrut(8));
        searchRow.add(addBtn);
        searchRow.add(Box.createHorizontalGlue());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(searchRow);

        // 关闭按钮
        StyledButton closeBtn = new StyledButton(
            "关 闭",
            new Color(0xC8CDD4),
            new Color(0xB0B8C1),
            UIConstants.TEXT_PRIMARY
        );
        closeBtn.setPreferredSize(new Dimension(0, 36));
        closeBtn.addActionListener(e -> dispose());

        root.add(title, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(closeBtn, BorderLayout.SOUTH);

        setContentPane(root);
        SwingUtilities.invokeLater(() -> searchField.requestFocus());
    }

    private void doAdd() {
        String curUser = SessionManager.getInstance().getNickname();
        String target = searchField.getText().trim();
        if (target.isEmpty()) {
            setResult("请输入用户名", false);
            return;
        }

        new SwingWorker<ApiResponse<Void>, Void>() {
            @Override
            protected ApiResponse<Void> doInBackground() throws Exception {
                Map<String, String> body = Map.of(
                    "fromUserNickname",
                    curUser,
                    "toUserNickname",
                    target
                );
                return ApiClient.post(
                    "/friends/requestswing",
                    body,
                    new TypeReference<ApiResponse<Void>>() {}
                );
            }

            @Override
            protected void done() {
                try {
                    ApiResponse<Void> resp = get();
                    int code = resp.getCode();
                    setResult(resp.getMessage(), code == 404 ? false : true);
                } catch (ExecutionException e) {
                    System.out.println(e);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }.execute();

        // if (success) {
        //     onAdded.run();
        //     searchField.setText("");
        // }
    }

    private void setResult(String msg, boolean success) {
        title.setText(msg);
        title.setForeground(
            success ? new Color(0x18A058) : new Color(0xFF6666)
        );
    }
}
