# ChatApp - Java Swing 聊天软件

基于 Java Swing + FlatIntelliJLaf 浅色主题的桌面聊天应用。

## 功能
- 🔐 登录 / 注册
- 💬 好友聊天（气泡消息）
- 👤 添加好友
- 🌙 FlatLaf 深色主题

## 环境要求
- JDK 11+
- Maven 3.6+

## 快速启动

```bash
# 1. 安装依赖并编译
mvn clean package -DskipTests

# 2. 运行
java -jar target/ChatApp-1.0.0.jar
```

## 演示账号
| 用户名 | 密码   | 昵称  |
|--------|--------|-------|
| admin  | 123456 | 管理员 |
| alice  | 123456 | Alice |
| bob    | 123456 | Bob   |

admin 与 alice、bob 已互为好友，可直接登录聊天。

## 项目结构

```
src/main/java/com/chatapp/
├── Main.java                   # 入口，初始化 FlatLaf
├── model/
│   ├── User.java               # 用户模型
│   └── Message.java            # 消息模型
├── service/
│   ├── UserService.java        # 用户服务（登录/注册/好友）
│   └── MessageService.java     # 消息存储服务
├── ui/
│   ├── login/
│   │   └── LoginFrame.java     # 登录/注册窗口
│   ├── chat/
│   │   ├── MainFrame.java      # 主窗口
│   │   ├── FriendListPanel.java # 左侧好友列表
│   │   └── ChatPanel.java      # 右侧聊天区
│   └── friend/
│       └── AddFriendDialog.java # 添加好友弹窗
└── util/
    ├── UIConstants.java        # 颜色/字体常量
    ├── RoundedPanel.java       # 圆角面板
    ├── StyledButton.java       # 自定义按钮
    └── AvatarPanel.java        # 圆形头像
```

## 扩展建议
- **持久化**：引入 SQLite (sqlite-jdbc) 替代内存存储
- **网络聊天**：增加 Socket 服务端实现真实通信
- **消息通知**：SystemTray 托盘通知
- **图片/文件传输**：扩展 Message 类型
