package top.chenray.qlogin;

import top.chenray.qlogin.command.*;
import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.handler.PlayerHandler;
import top.chenray.qlogin.util.LanguageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * QLogin 主入口 - Fabric Mod (Mojang 映射版, Minecraft 26.2)
 */
public class LoginMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("QLogin");

    /** 配置目录 */
    private static Path configDir;

    /** Minecraft Server 实例 */
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        LOGGER.info("QLogin 登录系统初始化中... (Mojang 映射版)");

        // 1. 获取配置目录
        configDir = FabricLoader.getInstance().getConfigDir().resolve("loginmod");
        LOGGER.info("配置目录: {}", configDir);

        // 2. 加载配置
        ModConfig.load(configDir);

        // 3. 初始化语言管理器
        LanguageManager.init();

        // 4. 初始化数据库
        DatabaseManager.init(configDir);
        DatabaseManager.getInstance().connect();

        // 5. 注册事件处理器
        PlayerHandler.register();

        // 6. 注册命令
        CommandRegistrationCallback.EVENT.register(AdminCommand::register);
        CommandRegistrationCallback.EVENT.register(RegisterCommand::register);
        CommandRegistrationCallback.EVENT.register(LoginCommand::register);
        CommandRegistrationCallback.EVENT.register(LogoutCommand::register);
        CommandRegistrationCallback.EVENT.register(ChangePasswordCommand::register);

        LOGGER.info("QLogin 登录系统初始化完成！");
    }

    /**
     * 服务器启动后的回调
     */
    public static void onServerStarted(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

    /**
     * 获取服务器实例
     */
    public static MinecraftServer getServer() {
        return server;
    }

    /**
     * 获取配置目录
     */
    public static Path getConfigDir() {
        return configDir;
    }
}
