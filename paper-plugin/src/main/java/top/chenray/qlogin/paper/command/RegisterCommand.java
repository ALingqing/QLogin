package top.chenray.qlogin.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.chenray.qlogin.paper.AuthManager;

public class RegisterCommand implements CommandExecutor {

    private final AuthManager authManager;

    public RegisterCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§c用法: /register <密码> <确认密码>");
            return true;
        }

        if (authManager.isRegistered(player)) {
            player.sendMessage("§c该账号已注册，请使用 /login 登录");
            return true;
        }

        if (!args[0].equals(args[1])) {
            player.sendMessage("§c两次输入的密码不一致");
            return true;
        }

        if (args[0].length() < 4) {
            player.sendMessage("§c密码长度不能少于4位");
            return true;
        }

        if (authManager.register(player, args[0])) {
            player.sendMessage("§a注册成功！已自动登录");
        } else {
            player.sendMessage("§c注册失败，请联系管理员");
        }
        return true;
    }
}
