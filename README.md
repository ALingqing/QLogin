# QLogin

QLogin 是一款基于 Fabric 的服务端登录认证 Mod，专为 Minecraft 26.2 设计，用于保护服务器免受未经授权的访问。

## 核心功能

- **注册与登录**：玩家首次进入服务器需使用 `/register <密码> <确认密码>` 注册账号，之后使用 `/login <密码>` 登录。
- **登录保护**：未登录的玩家无法移动、破坏/放置方块、与实体交互、发送聊天消息或执行任何命令，从根源上防止恶意破坏。
- **密码安全**：采用 BCrypt 算法加密存储密码，并支持设置密码长度范围（默认 4-32 位）。
- **登录超时**：玩家登录超时后自动踢出，也可配置为切换到旁观模式观看其他人游戏。
- **IP 封禁**：连续登录失败达到上限后自动临时封禁 IP，防止暴力破解密码。
- **密码修改**：玩家可通过 `/changepassword <旧密码> <新密码>` 自助修改登录密码。
- **多语言支持**：内置中文和英文两种语言，可通过配置文件热切换，方便国内外玩家使用。
- **管理员工具**：提供 `/qadmin reload`（重载配置）、`/qadmin unregister <玩家>`（强制注销）、`/qadmin resetpassword <玩家>`（重置密码）、`/qadmin info <玩家>`（查看注册信息）等管理命令。

## 配置文件

配置文件位于 `config/loginmod/loginmod.json`，可自定义以下参数：

- 登录超时时间（默认 60 秒）
- 最大登录尝试次数（默认 5 次）
- IP 封禁时长（默认 300 秒）
- 超时时是否踢出或切换到旁观模式
- 密码最小/最大长度
- 语言选择（zh_cn / en_us）

## 安装方式

1. 服务端需安装 Fabric Loader 0.19.0 以上版本和 Fabric API。
2. 将 QLogin 的 jar 文件放入服务端的 mods 文件夹。
3. 启动服务器，Mod 会自动生成配置文件。
4. 根据需求修改配置后重启服务器即可生效。

## 技术信息

- 运行环境：Minecraft 26.2 (Mojang Mappings)
- 框架：Fabric Loader >= 0.19.0 + Fabric API
- Java 版本：>= 25
- 密码加密：BCrypt
- 数据库：SQLite / H2
- 开源协议：MIT License
- 作者：aqing

---

# QLogin

QLogin is a Fabric server-side authentication mod designed for Minecraft 26.2, protecting your server from unauthorized access.

## Core Features

- **Register & Login**: New players register with `/register <password> <confirm>`, then login with `/login <password>`.
- **Login Protection**: Unauthenticated players cannot move, break/place blocks, interact with entities, chat, or execute any commands.
- **Password Security**: Passwords are hashed with BCrypt and stored securely. Configurable password length limits (default 4-32 characters).
- **Login Timeout**: Players who fail to login in time are kicked, or can be switched to spectator mode.
- **IP Ban**: Automatically temporarily bans IP addresses after too many failed login attempts, preventing brute force attacks.
- **Password Change**: Players can change their password anytime with `/changepassword <old> <new>`.
- **Multi-language**: Built-in Chinese and English language support, hot-swappable via config file.
- **Admin Tools**: Includes `/qadmin reload` (reload config), `/qadmin unregister <player>` (force unregister), `/qadmin resetpassword <player>` (reset password), and `/qadmin info <player>` (view registration info).

## Configuration

File location: `config/loginmod/loginmod.json`. Customizable options include:

- Login timeout duration (default 60 seconds)
- Max login attempts (default 5)
- IP ban duration (default 300 seconds)
- Kick or spectator mode on timeout
- Password min/max length
- Language selection (zh_cn / en_us)

## Installation

1. Install Fabric Loader >= 0.19.0 and Fabric API on your server.
2. Place the QLogin jar file into the mods folder.
3. Start the server; the config file will be generated automatically.
4. Modify the config as needed and restart the server.

## Technical Details

- Environment: Minecraft 26.2 (Mojang Mappings)
- Framework: Fabric Loader >= 0.19.0 + Fabric API
- Java: >= 25
- Password Hashing: BCrypt
- Database: SQLite / H2
- License: MIT
- Author: aqing