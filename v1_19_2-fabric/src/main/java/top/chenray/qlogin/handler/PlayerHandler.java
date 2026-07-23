package top.chenray.qlogin.handler;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.util.TextUtils;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

/**
 * 玩家事件处理器 - Fabric 1.19.2
 */
public class PlayerHandler {

    private static final Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;

    public static void register() {
        LoginManager loginManager = LoginManager.getInstance();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            loginManager.recordLoginPosition(player);
            loginManager.onPlayerJoin(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            loginManager.onPlayerDisconnect(handler.getPlayer());
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                return loginManager.isLoggedIn(serverPlayer.getUuid());
            }
            return true;
        });
    }

    public static void onServerTick(MinecraftServer server) {
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.cleanExpiredBans();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            LoginState state = loginManager.getState(player.getUuid());
            if (state == LoginState.LOGGED_IN) continue;

            if (loginManager.isLoginTimeout(player.getUuid())) {
                player.networkHandler.disconnect(Text.literal(TextUtils.t("login.timeout_kick")));
                LOGGER.warn("Player {} login timeout, kicked", player.getName().getString());
                continue;
            }

            double[] loginPos = loginManager.getLoginPosition(player.getUuid());
            if (loginPos != null) {
                double dx = player.getX() - loginPos[0];
                double dz = player.getZ() - loginPos[2];
                if (Math.abs(dx) > 0.5 || Math.abs(dz) > 0.5) {
                    player.teleport(server.getOverworld(),
                        loginPos[0], loginPos[1], loginPos[2],
                        (float) loginPos[3], (float) loginPos[4]);
                }
                if (player.getY() < -50) {
                    player.teleport(server.getOverworld(),
                        loginPos[0], loginPos[1], loginPos[2],
                        (float) loginPos[3], (float) loginPos[4]);
                    player.setHealth(player.getMaxHealth());
                    player.getHungerManager().setFoodLevel(20);
                }
            }

            long remaining = loginManager.getRemainingTime(player.getUuid());
            if (remaining > 0 && remaining % 10 == 0) {
                if (state == LoginState.UNREGISTERED) {
                    TextUtils.sendActionBar(player, "actionbar.register", String.valueOf(remaining));
                } else {
                    TextUtils.sendActionBar(player, "actionbar.login", String.valueOf(remaining));
                }
            } else if (remaining <= 5 && remaining > 0) {
                if (state == LoginState.UNREGISTERED) {
                    TextUtils.sendActionBar(player, "actionbar.urgent",
                        TextUtils.t("actionbar.register_urgent"), remaining);
                } else {
                    TextUtils.sendActionBar(player, "actionbar.urgent",
                        TextUtils.t("actionbar.login_urgent"), remaining);
                }
            }
        }
    }
}
