package top.chenray.qlogin.handler;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.util.TextUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/**
 * 玩家事件处理器 - 处理加入、离开、Tick 等事件 (Mojang 映射版)
 */
public class PlayerHandler {

    private static final Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;

    /**
     * 注册所有事件监听器
     */
    public static void register() {
        // 玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            LoginManager.getInstance().onPlayerJoin(player);
        });

        // 玩家离开事件
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            LoginManager.getInstance().onPlayerDisconnect(player);
        });

        // 服务器 Tick 事件（用于超时检测和 ActionBar 提示）
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 仅在配置启用了超时踢出时才检测
            if (!ModConfig.getInstance().isKickOnTimeout()) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                LoginManager lm = LoginManager.getInstance();
                LoginState state = lm.getState(player.getUUID());

                if (state == LoginState.LOGGED_IN) continue;

                // 超时检测
                if (lm.isLoginTimeout(player.getUUID())) {
                    String kickMsg = TextUtils.t("login.timeout_kick");
                    player.connection.disconnect(Component.literal(kickMsg));
                    LOGGER.info("玩家 {} 登录超时，已断开连接", player.getScoreboardName());
                    continue;
                }

                // ActionBar 提示
                long remaining = lm.getRemainingTime(player.getUUID());
                String timeStr = formatTime(remaining);

                if (state == LoginState.UNREGISTERED) {
                    if (remaining <= 10) {
                        TextUtils.sendActionBar(player, "actionbar.urgent",
                            TextUtils.t("actionbar.register_urgent"), remaining);
                    } else {
                        TextUtils.sendActionBar(player, "actionbar.register", timeStr);
                    }
                } else {
                    if (remaining <= 10) {
                        TextUtils.sendActionBar(player, "actionbar.urgent",
                            TextUtils.t("actionbar.login_urgent"), remaining);
                    } else {
                        TextUtils.sendActionBar(player, "actionbar.login", timeStr);
                    }
                }
            }
        });

        // 服务器启动事件（用于初始化）
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            top.chenray.qlogin.LoginMod.onServerStarted(server);
            LOGGER.info("QLogin 登录系统已加载 (Mojang 映射版)");
        });

        LOGGER.info("玩家事件处理器已注册");
    }

    /**
     * 格式化时间为 MM:SS
     */
    private static String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
