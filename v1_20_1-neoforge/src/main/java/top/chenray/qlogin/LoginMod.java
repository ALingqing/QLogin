package top.chenray.qlogin;

import top.chenray.qlogin.command.*;
import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.handler.PlayerHandler;
import top.chenray.qlogin.util.LanguageManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * QLogin 登录系统 - NeoForge 1.20.1 版本
 */
@Mod(LoginMod.MOD_ID)
public class LoginMod {

    public static final String MOD_ID = "qlogin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static MinecraftServer server;

    public static MinecraftServer getServer() { return server; }

    public LoginMod(IEventBus modEventBus) {
        LOGGER.info("======================================");
        LOGGER.info("  QLogin 登录系统 v{} (1.20.1 NeoForge)", "1.0.0");
        LOGGER.info("======================================");

        modEventBus.addListener(this::onCommonSetup);

        var eventBus = NeoForge.EVENT_BUS;
        eventBus.addListener(this::onServerStarting);
        eventBus.addListener(this::onRegisterCommands);
        eventBus.addListener(this::onServerStopping);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("QLogin 通用初始化完成");
    }

    private void onServerStarting(ServerStartingEvent event) {
        long startTime = System.currentTimeMillis();
        server = event.getServer();

        Path configDir = server.getServerDirectory().toPath().resolve("config").resolve("loginmod");

        ModConfig.load(configDir);
        LanguageManager.init();
        DatabaseManager.init(configDir);
        DatabaseManager.getInstance().connect();

        NeoForge.EVENT_BUS.register(new PlayerHandler());

        LOGGER.info("QLogin 初始化完成 ({}ms)", System.currentTimeMillis() - startTime);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        RegisterCommand.register(dispatcher);
        LoginCommand.register(dispatcher);
        LogoutCommand.register(dispatcher);
        ChangePasswordCommand.register(dispatcher);
        AdminCommand.register(dispatcher);

        LOGGER.info("QLogin 命令已注册");
        LOGGER.info("QLogin 登录系统加载完成 (1.20.1 NeoForge)");
    }

    private void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("正在关闭登录系统...");
        DatabaseManager.getInstance().close();
        LOGGER.info("登录系统已关闭");
    }
}
