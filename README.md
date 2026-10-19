# Simple Chat · Java Swing 聊天客户端

基于 **Java Swing + FlatLaf** 的桌面聊天客户端，采用浅色界面，提供好友列表、文本聊天、好友申请处理及声音提示。

客户端通过 **HTTP API** 完成登录、好友管理和消息发送，通过 **TCP Socket** 接收实时消息及状态通知。

> 本仓库仅包含客户端，不包含后端服务或数据库初始化脚本。打开登录窗口不需要后端，但登录、聊天和好友管理需要兼容的后端服务。

## 功能状态

| 功能                | 当前实现                                             |
| ------------------- | ---------------------------------------------------- |
| 用户登录 / 退出登录 | 调用后端接口，登录后保存内存会话，退出后返回登录窗口 |
| 好友列表            | 显示昵称、头像和在线 / 离线状态，按状态排序          |
| 一对一文本聊天      | 消息气泡、多行文本、发送时间及自动滚动               |
| 历史消息            | 登录进入主窗口后从后端加载，并按会话缓存在内存中     |
| 实时通知            | 通过 TCP 接收消息、用户上下线和好友事件              |
| 好友申请            | 发送申请，登录后检查待处理申请，支持接受 / 拒绝      |
| 声音提示            | 新消息、好友上线及好友申请提示音                     |
| 用户注册            | **尚未接入**，注册页仅展示未实现提示                 |

当前使用 `FlatIntelliJLaf` 浅色主题，不提供深色主题切换。暂不支持群聊、图片 / 文件传输和消息全文搜索。

## 技术栈

| 技术            | 版本 / 用途                             |
| --------------- | --------------------------------------- |
| Java            | 编译目标为 Java 21                      |
| Swing / AWT     | 桌面界面、自定义组件和绘制              |
| FlatLaf         | 3.4，界面主题                           |
| Java HttpClient | HTTP 请求                               |
| Java Socket     | TCP 长连接与实时事件接收                |
| Jackson         | 2.17.0，JSON 序列化及 Java 时间类型支持 |
| JLayer          | 1.0.1，MP3 提示音播放                   |
| Maven           | 依赖管理、编译与打包                    |

网络请求主要使用 `SwingWorker` 在后台执行，界面更新在 Swing 事件分发线程中进行。

## 环境要求

- **JDK 21 或更高版本**，确保 `java` 和 Maven 使用兼容的 JDK。
- Maven；建议使用 Maven 3.9.x，仓库未提供 Maven Wrapper。
- 可用的桌面图形环境，不适用于无图形界面的服务器运行模式。
- 兼容的 HTTP API 服务和 TCP 消息服务。
- 首次构建需要访问 Maven 仓库下载依赖。

检查本地环境：

```bash
java -version
mvn -version
```

## 快速开始

以下命令均在项目根目录执行。

### 1. 准备后端

默认连接地址如下：

| 通道       | 默认地址                    | 配置位置                                                                              |
| ---------- | --------------------------- | ------------------------------------------------------------------------------------- |
| HTTP API   | `http://localhost:8080/api` | `src/main/java/com/chatapp/spring/api/ApiClient.java` 中的 `BASE_URL`                 |
| TCP Socket | `localhost:8412`            | `src/main/java/com/chatapp/ui/chat/MainFrame.java` 中的 `messageService.connect(...)` |

请先启动对应的后端服务，并准备有效的用户账号。如需连接其他服务器，修改上述两处地址后重新编译。当前尚未提供环境变量、命令行参数或外部配置文件来覆盖连接地址。

> `localhost` 指运行客户端的电脑。跨设备使用时，HTTP 和 TCP 地址都需要改为实际服务端地址，并确保两个端口可访问。TCP 通道是普通 Socket，不是 WebSocket。

### 2. 编译与打包

```bash
mvn clean package
```

构建后的主要产物：

```text
target/
├── ChatApp-1.0.0.jar
└── lib/                    # 运行所需的第三方依赖
```

### 3. 启动客户端

```bash
java -jar target/ChatApp-1.0.0.jar
```

也可以在 IDE 中以项目根目录为工作目录，运行入口类 `com.chatapp.Main`。

**打包注意事项：**

- 当前不是包含所有依赖的单文件 JAR，运行时必须保留 `target/lib/` 与 JAR 的相对位置。
- Logo 和头像仍通过 `src/main/resources/...` 相对文件路径读取，因此建议从项目根目录启动。
- 仅复制 JAR 和 `lib/` 到其他目录，不能保证图片正常显示；独立分发前需要将相关图片加载方式改为类路径资源读取，或保留相应资源目录结构。

## 使用说明

1. 输入后端已有账号和密码登录；密码框支持按 `Enter` 提交。
2. 登录后，左侧显示好友列表，右上角显示当前用户信息。
3. 选择好友后查看会话，输入文本并点击「发送」。
4. macOS 下支持 `Command + Enter` 发送；普通 `Enter` 用于换行。其他平台可使用发送按钮，当前未绑定 `Ctrl + Enter`。
5. 点击「+ 好友」提交好友申请，对方可在好友验证弹窗中接受或拒绝。
6. 点击「× 退出」注销会话、断开 TCP 连接并清空本地聊天缓存。

### 账号说明

实际登录调用后端 `/auth/login`，账号及好友关系以服务端数据为准。

`UserService` 中保留了 `admin`、`alice`、`bob` 等旧版内存演示数据，但当前登录流程不使用该服务进行认证，**这些账号不保证可以登录后端**。请由服务端创建测试账号；当前客户端注册页不可用于创建账号。

## 后端接口约定

以下路径均相对于 `/api`，依据当前客户端调用整理，不代表本仓库提供了对应服务端实现。POST 请求使用 JSON 请求体。

| 方法 | 路径                          | 参数 / 请求体                        | 用途                       |
| ---- | ----------------------------- | ------------------------------------ | -------------------------- |
| POST | `/auth/login`                 | `username`、`password`               | 登录，返回用户信息         |
| POST | `/auth/logout`                | `username`                           | 退出登录                   |
| GET  | `/friends/get`                | 查询参数 `userId`：当前用户 ID       | 获取好友列表               |
| POST | `/friends/requestswing`       | `fromUserNickname`、`toUserNickname` | 发送好友申请               |
| GET  | `/friends/pending-requests`   | 查询参数 `userId`：当前用户名        | 获取待处理申请             |
| POST | `/friends/accepted-swing`     | `fromUserId`、`toUserId`、`recordId` | 接受好友申请               |
| POST | `/friends/requests/mark-read` | 查询参数 `recordId`                  | 拒绝申请时将记录标记为已读 |
| POST | `/chat/send/swing`            | `fromUserId`、`toUserId`、`content`  | 发送文本消息               |
| GET  | `/chat/history`               | 查询参数 `userId`：当前用户 ID       | 获取历史消息               |

**对接时需要注意：**

- 添加好友界面提示输入「用户名」，但实际请求使用的是 `fromUserNickname` / `toUserNickname` 字段，请核对后端的查找规则。
- 待处理申请接口的参数虽然叫 `userId`，客户端实际传入的是用户名，不是数字 ID。
- 当前请求未携带认证 Token；生产使用前需完善服务端身份认证与权限校验。

### JSON 响应结构

需要解析业务响应的接口使用 `ApiResponse<T>`：

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

`data` 的具体类型随接口而变化，例如：

- 用户信息：`id`、`username`、`nickname`、`avatar`、`status`，其中 `status = 1` 显示为在线。
- 消息信息：`id`、`fromUserId`、`toUserId`、`content`、`sendTime`、`isRead`。
- 好友列表、历史消息和待处理申请返回相应对象数组。

### TCP 实时协议

客户端按行读取服务端数据，使用 `|` 分隔协议字段。当前使用的主要消息格式为：

```text
客户端 → 服务端
LOGIN|<username>
LOGOUT

服务端 → 客户端
MSG|<消息 JSON>
SERVER|USER_LOGIN|<username>
SERVER|USER_LOGOUT
SERVER|FRIEND_REQUEST
SERVER|FRIEND_ACCEPT
```

消息发送本身走 HTTP，TCP 负责实时接收和事件通知。当前解析使用 `split("\\|", -1)`，消息 JSON 中若包含 `|` 会影响字段拆分，对接时应注意这一限制。

## 项目结构

```text
.
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/chatapp/
    │   ├── Main.java                 # 程序入口、FlatLaf 初始化
    │   ├── model/                    # 旧版本地用户、消息模型
    │   ├── service/
    │   │   ├── MessageService.java   # TCP 连接、消息发送及历史缓存
    │   │   └── UserService.java      # 旧版内存用户服务
    │   ├── spring/
    │   │   ├── api/                  # HTTP 客户端及接口异常
    │   │   └── res/                  # API 响应 DTO
    │   ├── ui/
    │   │   ├── login/                # 登录及注册占位页
    │   │   ├── chat/                 # 主窗口、好友列表、聊天面板
    │   │   └── friend/               # 添加好友、好友验证弹窗
    │   └── util/                     # 会话、字体、头像、音频及 UI 组件
    └── resources/
        ├── DingTalk-JinBuTi.ttf       # 自定义字体
        ├── chat-icon.png             # 应用 Logo
        ├── icons/                    # 预置头像
        └── audio/                    # MP3 提示音
```

`spring` 是客户端接口适配相关的包名，本项目没有引入 Spring / Spring Boot，也不包含服务端启动入口。

## 常见问题

### 编译提示 `invalid target release: 21`

Maven 使用的 JDK 版本过低。检查 `mvn -version`，将 Maven 的 Java 环境切换到 JDK 21 或更高版本后重新构建。

### 登录提示网络连接失败

检查 HTTP 后端是否启动、`BASE_URL` 是否正确，以及端口 `8080` 是否可访问。用户名和密码必须由实际后端提供。

### 登录成功，但提示「连接服务器失败」或没有实时通知

HTTP 和 TCP 是两条独立连接。请检查 TCP 服务是否监听 `8412`，以及 `MainFrame` 中的主机和端口配置。当前没有自动重连机制。

### 启动后缺少依赖或图片不显示

请使用 `mvn clean package` 完整打包，确认 `target/lib/` 存在，并从项目根目录运行。图片显示还依赖源码中的相对资源路径，详见「打包注意事项」。

### 注册页为什么不能注册？

注册界面尚未实现后端对接。代码中残留的内存注册逻辑不等同于服务端注册，无法为当前 HTTP 登录流程创建账号。

## 当前限制与后续方向

- 本地会话和聊天缓存仅保存在内存中，没有客户端数据库；历史消息恢复依赖后端接口。
- 当前默认使用明文 HTTP 和 TCP，尚无 Token 认证、TLS 或自动重连，不宜直接用于不可信网络。
- 消息会先加入本地缓存再异步发送，尚无完善的发送失败提示、重试和送达确认。
- 后续可完善注册、统一外部配置、类路径资源加载、TCP 协议转义和自动化测试。

目前仓库未包含自动化测试；打包成功并不代表已完成后端联调，登录、消息收发及好友流程仍需配合实际服务端验证。
