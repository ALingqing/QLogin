package top.chenray.qlogin.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.chenray.qlogin.paper.AuthManager;

public class LoginCommand implements CommandExecutor {

    private final AuthManager authManager;

    public LoginCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§c用法: /login <密码>");
            return true;
        }

        if (authManager.isLoggedIn(player)) {
            player.sendMessage("§a你已经登录了");
            return true;
        }

        if (!authManager.isRegistered(player)) {
            player.sendMessage("§c该账号未注册，请先使用 /register <密码> <确认密码> 注册");
            return true;
        }

        if (authManager.login(player, args[0])) {
            player.sendMessage("§a登录成功！欢迎回来");
        } else {
            player.sendMessage("§c密码错误");
        }
        return true;
    }
}
