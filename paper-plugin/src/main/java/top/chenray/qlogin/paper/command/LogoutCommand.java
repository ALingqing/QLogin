package top.chenray.qlogin.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.chenray.qlogin.paper.AuthManager;

public class LogoutCommand implements CommandExecutor {

    private final AuthManager authManager;

    public LogoutCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令");
            return true;
        }
        Player player = (Player) sender;

        if (!authManager.isLoggedIn(player)) {
            player.sendMessage("§c你还未登录");
            return true;
        }

        authManager.logout(player);
        player.sendMessage("§a已退出登录");
        return true;
    }
}
