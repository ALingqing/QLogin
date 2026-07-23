package top.chenray.qlogin.handler;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.util.TextUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import org.slf4j.Logger;

/**
 * 玩家事件处理器 - Forge 1.19.2
 */
public class PlayerHandler {

    private static final Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LoginManager.getInstance().recordLoginPosition(player);
            LoginManager.getInstance().onPlayerJoin(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LoginManager.getInstance().onPlayerDisconnect(player);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != Phase.END) return;

        LoginManager loginManager = LoginManager.getInstance();
        loginManager.cleanExpiredBans();

        MinecraftServer server = top.chenray.qlogin.LoginMod.getServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            LoginState state = loginManager.getState(player.getUUID());
            if (state == LoginState.LOGGED_IN) continue;

            if (loginManager.isLoginTimeout(player.getUUID())) {
                player.connection.disconnect(Component.literal(TextUtils.t("login.timeout_kick")));
                LOGGER.warn("Player {} login timeout, kicked", player.getDisplayName().getString());
                continue;
            }

            double[] loginPos = loginManager.getLoginPosition(player.getUUID());
            if (loginPos != null) {
                double dx = player.getX() - loginPos[0];
                double dz = player.getZ() - loginPos[2];
                if (Math.abs(dx) > 0.5 || Math.abs(dz) > 0.5) {
                    player.teleportTo(loginPos[0], loginPos[1], loginPos[2]);
                    player.setYRot((float) loginPos[3]);
                    player.setXRot((float) loginPos[4]);
                }
                if (player.getY() < -50) {
                    player.teleportTo(loginPos[0], loginPos[1], loginPos[2]);
                    player.setYRot((float) loginPos[3]);
                    player.setXRot((float) loginPos[4]);
                    player.setHealth(player.getMaxHealth());
                    player.getFoodData().setFoodLevel(20);
                }
            }

            long remaining = loginManager.getRemainingTime(player.getUUID());
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

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player.getUUID())) {
                if (event.getSide() == LogicalSide.SERVER) {
                    player.sendSystemMessage(Component.literal(TextUtils.t("interact.blocked")));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player.getUUID())) {
                if (event.getSide() == LogicalSide.SERVER) {
                    player.sendSystemMessage(Component.literal(TextUtils.t("interact.entity_blocked")));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!LoginManager.getInstance().isLoggedIn(player.getUUID())) {
            player.sendSystemMessage(Component.literal(TextUtils.t("chat.blocked")));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LoginManager loginManager = LoginManager.getInstance();
            LoginState state = loginManager.getState(player.getUUID());
            if (state == LoginState.LOGGED_IN) return;
            double[] loginPos = loginManager.getLoginPosition(player.getUUID());
            if (loginPos != null && loginManager.isPlayerFrozen(player.getUUID())) {
                player.teleportTo(loginPos[0], loginPos[1], loginPos[2]);
                player.setYRot((float) loginPos[3]);
                player.setXRot((float) loginPos[4]);
            }
        }
    }
}
