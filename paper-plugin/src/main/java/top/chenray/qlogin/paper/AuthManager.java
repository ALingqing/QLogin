package top.chenray.qlogin.paper;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthManager {

    private final QLoginPaper plugin;
    private final Set<UUID> loggedInPlayers = new HashSet<>();

    public AuthManager(QLoginPaper plugin) {
        this.plugin = plugin;
    }

    /**
     * 注册
     */
    public boolean register(Player player, String password) {
        String uuid = player.getUniqueId().toString();
        if (plugin.getDatabaseManager().isRegistered(uuid)) {
            return false; // 已注册
        }
        String hash = PasswordHasher.createPasswordHash(password);
        boolean success = plugin.getDatabaseManager().registerPlayer(uuid, player.getName(), hash);
        if (success) {
            loggedInPlayers.add(player.getUniqueId());
        }
        return success;
    }

    /**
     * 登录
     */
    public boolean login(Player player, String password) {
        String uuid = player.getUniqueId().toString();
        String storedHash = plugin.getDatabaseManager().getPasswordHash(uuid);
        if (storedHash == null) {
            return false; // 未注册
        }
        if (PasswordHasher.verifyPassword(password, storedHash)) {
            loggedInPlayers.add(player.getUniqueId());
            plugin.getDatabaseManager().updateLastLogin(uuid);
            return true;
        }
        return false;
    }

    /**
     * 退出登录
     */
    public void logout(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
    }

    /**
     * 是否已登录
     */
    public boolean isLoggedIn(Player player) {
        return loggedInPlayers.contains(player.getUniqueId());
    }

    /**
     * 是否已注册
     */
    public boolean isRegistered(Player player) {
        return plugin.getDatabaseManager().isRegistered(player.getUniqueId().toString());
    }

    /**
     * 修改密码
     */
    public boolean changePassword(Player player, String oldPassword, String newPassword) {
        String uuid = player.getUniqueId().toString();
        String storedHash = plugin.getDatabaseManager().getPasswordHash(uuid);
        if (storedHash == null) return false;
        if (!PasswordHasher.verifyPassword(oldPassword, storedHash)) return false;
        String newHash = PasswordHasher.createPasswordHash(newPassword);
        return plugin.getDatabaseManager().updatePassword(uuid, newHash);
    }
}
