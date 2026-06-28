package top.chenray.qlogin.paper;

import org.bukkit.plugin.java.JavaPlugin;
import top.chenray.qlogin.paper.command.*;
import top.chenray.qlogin.paper.listener.*;

public class QLoginPaper extends JavaPlugin {

    private static QLoginPaper instance;
    private AuthManager authManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // 保存默认配置
        saveDefaultConfig();

        // 初始化数据库
        databaseManager = new DatabaseManager(this);
        databaseManager.init();

        // 初始化认证管理器
        authManager = new AuthManager(this);

        // 注册命令
        getCommand("login").setExecutor(new LoginCommand(authManager));
        getCommand("register").setExecutor(new RegisterCommand(authManager));
        getCommand("logout").setExecutor(new LogoutCommand(authManager));
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(authManager));

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(authManager), this);

        getLogger().info("QLoginPaper 已启用 v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("QLoginPaper 已禁用");
    }

    public static QLoginPaper getInstance() {
        return instance;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
