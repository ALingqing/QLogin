package top.chenray.qlogin;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * QLogin 登录系统 - Forge 1.19.2 版本
 * 使用 Mojang 官方映射 (official mapping)
 */
@Mod(LoginMod.MOD_ID)
public class LoginMod {

    public static final String MOD_ID = "qlogin";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static MinecraftServer server;

    public static MinecraftServer getServer() {
        return server;
    }

    public LoginMod() {
        LOGGER.info("======================================");
        LOGGER.info("  QLogin 登录系统 v{} (1.19.2 Forge)", "1.0.0");
        LOGGER.info("======================================");

        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("QLogin 通用初始化完成");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        long startTime = System.currentTimeMillis();
        server = event.getServer();

        Path configDir = server.getServerDirectory().toPath().resolve("config").resolve("loginmod");

        top.chenray.qlogin.config.ModConfig.load(configDir);
        top.chenray.qlogin.util.LanguageManager.init();
        top.chenray.qlogin.database.DatabaseManager.init(configDir);
        top.chenray.qlogin.database.DatabaseManager.getInstance().connect();

        MinecraftForge.EVENT_BUS.register(new top.chenray.qlogin.handler.PlayerHandler());

        LOGGER.info("QLogin 初始化完成 ({}ms)", System.currentTimeMillis() - startTime);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        top.chenray.qlogin.command.RegisterCommand.register(dispatcher);
        top.chenray.qlogin.command.LoginCommand.register(dispatcher);
        top.chenray.qlogin.command.LogoutCommand.register(dispatcher);
        top.chenray.qlogin.command.ChangePasswordCommand.register(dispatcher);
        top.chenray.qlogin.command.AdminCommand.register(dispatcher);

        LOGGER.info("QLogin 命令已注册");
        LOGGER.info("QLogin 登录系统加载完成 (1.19.2 Forge)");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("正在关闭登录系统...");
        top.chenray.qlogin.database.DatabaseManager.getInstance().close();
        LOGGER.info("登录系统已关闭");
    }
}
