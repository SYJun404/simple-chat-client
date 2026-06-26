package com.chatapp.ui.login;

import com.chatapp.service.UserService;
import com.chatapp.spring.api.ApiClient;
import com.chatapp.spring.api.ApiException;
import com.chatapp.spring.res.*;
import com.chatapp.ui.chat.MainFrame;
import com.chatapp.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.*;

public class LoginFrame extends JFrame {

    private final UserService userService = UserService.getInstance();

    // 登录组件
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // 注册组件
    private JTextField regUsernameField;
    private JTextField regNicknameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmField;

    // 自定义 Tab 切换
    private JButton loginTabBtn;
    private JButton registerTabBtn;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public LoginFrame() {
        setTitle("");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(380, 480);
        setLocationRelativeTo(null);
        setBackground(UIConstants.BG_DARK);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG_DARK);
        root.setBorder(new EmptyBorder(40, 44, 36, 44));

        root.add(buildLogoPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Logo ──────────────────────────────────────────────
    private JPanel buildLogoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 28, 0));

        // 图标
        ImageIcon icon = loadIcon("src/main/resources/chat-icon.png", 56, 56);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 标题
        JLabel title = new JLabel("Simple Chat");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(title);

        return panel;
    }

    // ── 中部（Tab 切换 + 表单卡片）────────────────────────
    private JPanel buildCenterPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false); // 已有，确认保留

        wrapper.add(buildTabBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(UIConstants.BG_PANEL);
                g2.fill(
                    new RoundRectangle2D.Float(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        16,
                        16
                    )
                );
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(24, 20, 24, 20));

        cardPanel.add(buildLoginForm(), "login");
        cardPanel.add(buildRegisterForm(), "register");

        wrapper.add(cardPanel, BorderLayout.CENTER);
        return wrapper;
    }

    // ── 自定义 Tab 切换按钮 ────────────────────────────────
    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new GridLayout(1, 2));
        bar.setBackground(UIConstants.BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 2, 0));

        loginTabBtn = new JButton("登 录");
        registerTabBtn = new JButton("注 册");

        for (JButton btn : new JButton[] { loginTabBtn, registerTabBtn }) {
            btn.setFont(UIConstants.FONT_NORMAL);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(0, 38));
        }

        loginTabBtn.addActionListener(e -> switchTab(true));
        registerTabBtn.addActionListener(e -> switchTab(false));

        bar.add(loginTabBtn);
        bar.add(registerTabBtn);

        // 默认激活登录
        applyTabStyle(loginTabBtn, true);
        applyTabStyle(registerTabBtn, false);
        return bar;
    }

    private void switchTab(boolean isLogin) {
        cardLayout.show(cardPanel, isLogin ? "login" : "register");
        applyTabStyle(loginTabBtn, isLogin);
        applyTabStyle(registerTabBtn, !isLogin);
    }

    private void applyTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setForeground(UIConstants.ACCENT);
            btn.setFont(UIConstants.FONT_NORMAL.deriveFont(Font.BOLD));
            btn.setBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.ACCENT)
            );
        } else {
            btn.setForeground(UIConstants.TEXT_SECONDARY);
            btn.setFont(UIConstants.FONT_NORMAL);
            btn.setBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BG_DARK)
            );
        }
    }

    // ── 登录表单 ───────────────────────────────────────────
    private JPanel buildLoginForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        loginUsernameField = createTextField("请输入账号");
        loginPasswordField = createPasswordField("请输入密码");

        StyledButton loginBtn = new StyledButton(
            "登 录",
            UIConstants.ACCENT,
            UIConstants.ACCENT_HOVER
        );
        loginBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.addActionListener(e -> doLogin());

        loginPasswordField.addKeyListener(
            new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
                }
            }
        );

        panel.add(fieldLabel("账 号"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginUsernameField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(fieldLabel("密 码"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginPasswordField);
        panel.add(Box.createVerticalStrut(24));
        panel.add(loginBtn);
        return panel;
    }

    // ── 注册表单 ───────────────────────────────────────────
    private JPanel buildRegisterForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        regUsernameField = createTextField("当前功能还未实现...");
        // regNicknameField = createTextField("显示昵称");
        // regPasswordField = createPasswordField("设置密码（至少6位）");
        // regConfirmField = createPasswordField("确认密码");

        StyledButton regBtn = new StyledButton(
            "注 册",
            UIConstants.ACCENT,
            UIConstants.ACCENT_HOVER
        );
        regBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        regBtn.addActionListener(e -> doRegister());

        Object[][] rows = {
            { "当前功能还未实现...", regUsernameField },
            // { "昵　称", regNicknameField },
            // { "密　码", regPasswordField },
            // { "确认密码", regConfirmField },
        };

        for (Object[] row : rows) {
            panel.add(fieldLabel((String) row[0]));
            panel.add(Box.createVerticalStrut(5));
            // panel.add((Component) row[1]);
            // panel.add(Box.createVerticalStrut(11));
        }
        panel.add(Box.createVerticalStrut(8));
        // panel.add(regBtn);
        return panel;
    }

    private void doLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            showError("用户名和密码不能为空");
            return;
        }
        new SwingWorker<ApiResponse<UserResponse>, Void>() {
            @Override
            protected ApiResponse<UserResponse> doInBackground()
                throws Exception {
                Map<String, String> body = Map.of(
                    "username",
                    username,
                    "password",
                    password
                );
                return ApiClient.post(
                    "/auth/login",
                    body,
                    new TypeReference<ApiResponse<UserResponse>>() {}
                );
            }

            @Override
            protected void done() {
                try {
                    ApiResponse<UserResponse> resp = get();
                    if (resp.getCode() == 200) {
                        SessionManager.getInstance().login(
                            resp.getData().getId(),
                            resp.getData().getUsername(),
                            resp.getData().getNickname(),
                            resp.getData().getAvatar(),
                            resp.getData().getStatus()
                        );
                        dispose();
                        SwingUtilities.invokeLater(() ->
                            new MainFrame().setVisible(true)
                        );
                    } else {
                        showError(resp.getMessage());
                    }
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof ApiException) {
                        ApiException apiEx = (ApiException) cause;
                        showError(
                            apiEx.getStatusCode() == 401
                                ? "用户名或密码错误"
                                : "服务器错误：" + apiEx.getStatusCode()
                        );
                    } else {
                        showError("网络连接失败，请检查服务器");
                    }
                } catch (Exception e) {
                    showError("未知错误：" + e.getMessage());
                }
            }
        }.execute();
    }

    private void doRegister() {
        String username = regUsernameField.getText().trim();
        String nickname = regNicknameField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirm = new String(regConfirmField.getPassword());

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
            JOptionPane.showMessageDialog(
                this,
                "注册成功！请登录",
                "提示",
                JOptionPane.INFORMATION_MESSAGE
            );
            switchTab(true);
            loginUsernameField.setText(username);
        } else {
            showError("用户名已被使用");
        }
    }

    // ── 工具方法 ───────────────────────────────────────────
    private void showError(String msg) {
        JOptionPane.showMessageDialog(
            this,
            msg,
            "错误",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_SMALL);
        label.setForeground(UIConstants.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_NORMAL);
        field.setPreferredSize(new Dimension(0, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(UIConstants.FONT_NORMAL);
        field.setPreferredSize(new Dimension(0, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        ImageIcon icon = new ImageIcon(path);
        return new ImageIcon(
            icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)
        );
    }
}
