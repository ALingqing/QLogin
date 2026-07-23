package top.chenray.qlogin;

import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.util.TextUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录管理器 - 管理玩家登录状态、超时、IP 封禁
 */
public class LoginManager {

    private static final Logger LOGGER = LoginMod.LOGGER;
    private static LoginManager instance;

    private final Map<UUID, LoginState> playerStates = new ConcurrentHashMap<>();
    private final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();
    private final Map<UUID, double[]> loginPositions = new ConcurrentHashMap<>();
    private final Map<String, Long> bannedIps = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipFailCounts = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerIps = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> frozenPlayers = new ConcurrentHashMap<>();

    private LoginManager() {}

    public static synchronized LoginManager getInstance() {
        if (instance == null) instance = new LoginManager();
        return instance;
    }

    public void onPlayerJoin(ServerPlayer player) {
        UUID uuid = player.getUUID();
        DatabaseManager db = DatabaseManager.getInstance();
        String ip = getPlayerIp(player);
        playerIps.put(uuid, ip);

        if (isIpBanned(ip)) {
            player.connection.disconnect(Component.literal("§c§l您的 IP 已被封禁！"));
            return;
        }

        if (db.isPlayerRegistered(uuid)) {
            playerStates.put(uuid, LoginState.LOGGED_OUT);
        } else {
            playerStates.put(uuid, LoginState.UNREGISTERED);
        }

        joinTimes.put(uuid, System.currentTimeMillis());

        if (playerStates.get(uuid) == LoginState.LOGGED_OUT) {
            TextUtils.sendMsg(player, "login.registered");
        } else {
            TextUtils.sendMsg(player, "login.unregistered");
        }

        LOGGER.info("Player {} joined (IP: {}, State: {})",
            player.getName().getString(), ip, playerStates.get(uuid));
    }

    public void onPlayerDisconnect(ServerPlayer player) {
        UUID uuid = player.getUUID();
        playerStates.remove(uuid);
        joinTimes.remove(uuid);
        loginPositions.remove(uuid);
        playerIps.remove(uuid);
        frozenPlayers.remove(uuid);
    }

    public LoginState getState(UUID uuid) { return playerStates.get(uuid); }
    public void setLoggedIn(UUID uuid) { playerStates.put(uuid, LoginState.LOGGED_IN); }
    public boolean isLoggedIn(UUID uuid) { return playerStates.get(uuid) == LoginState.LOGGED_IN; }

    public void setLoggedOut(UUID uuid) {
        DatabaseManager db = DatabaseManager.getInstance();
        playerStates.put(uuid, db.isPlayerRegistered(uuid) ? LoginState.LOGGED_OUT : LoginState.UNREGISTERED);
    }

    public void recordLoginPosition(ServerPlayer player) {
        loginPositions.put(player.getUUID(), new double[]{
            player.getX(), player.getY(), player.getZ(),
            player.getYRot(), player.getXRot()
        });
    }

    public double[] getLoginPosition(UUID uuid) { return loginPositions.get(uuid); }

    public boolean isLoginTimeout(UUID uuid) {
        Long joinTime = joinTimes.get(uuid);
        if (joinTime == null) return false;
        return System.currentTimeMillis() - joinTime > ModConfig.getInstance().getLoginTimeoutSeconds() * 1000L;
    }

    public long getRemainingTime(UUID uuid) {
        Long joinTime = joinTimes.get(uuid);
        if (joinTime == null) return 0;
        long remaining = ModConfig.getInstance().getLoginTimeoutSeconds() - (System.currentTimeMillis() - joinTime) / 1000;
        return Math.max(0, remaining);
    }

    public String getPlayerIp(ServerPlayer player) {
        try {
            if (player.connection.connection != null) {
                var address = player.connection.connection.getRemoteAddress();
                if (address instanceof InetSocketAddress) {
                    return ((InetSocketAddress) address).getAddress().getHostAddress();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("无法获取玩家 IP: {}", e.getMessage());
        }
        return "unknown";
    }

    public String getPlayerIp(UUID uuid) { return playerIps.get(uuid); }

    public boolean isIpBanned(String ip) {
        Long banExpiry = bannedIps.get(ip);
        if (banExpiry == null) return false;
        if (System.currentTimeMillis() > banExpiry) {
            bannedIps.remove(ip);
            return false;
        }
        return true;
    }

    public void banIp(String ip) {
        bannedIps.put(ip, System.currentTimeMillis() + ModConfig.getInstance().getBanDurationSeconds() * 1000L);
        LOGGER.info("IP {} banned", ip);
    }

    public void cleanExpiredBans() {
        long now = System.currentTimeMillis();
        bannedIps.entrySet().removeIf(entry -> now > entry.getValue());
    }

    public int incrementLoginFails(UUID uuid) {
        String ip = playerIps.get(uuid);
        if (ip == null) return 0;
        int fails = ipFailCounts.getOrDefault(ip, 0) + 1;
        ipFailCounts.put(ip, fails);
        if (fails >= ModConfig.getInstance().getMaxLoginAttempts()) {
            banIp(ip);
            LOGGER.warn("IP {} banned due to too many failed attempts", ip);
        }
        return fails;
    }

    public void resetLoginFails(UUID uuid) {
        String ip = playerIps.get(uuid);
        if (ip != null) ipFailCounts.remove(ip);
    }

    public boolean isPlayerFrozen(UUID uuid) { return frozenPlayers.getOrDefault(uuid, false); }
    public void setPlayerFrozen(UUID uuid, boolean frozen) { frozenPlayers.put(uuid, frozen); }
}
