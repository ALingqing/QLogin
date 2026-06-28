package top.chenray.qlogin.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.chenray.qlogin.paper.AuthManager;

public class ChangePasswordCommand implements CommandExecutor {

    private final AuthManager authManager;

    public ChangePasswordCommand(AuthManager authManager) {
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
            player.sendMessage("§c用法: /changepassword <旧密码> <新密码>");
            return true;
        }

        if (!authManager.isLoggedIn(player)) {
            player.sendMessage("§c请先登录");
            return true;
        }

        if (args[1].length() < 4) {
            player.sendMessage("§c新密码长度不能少于4位");
            return true;
        }

        if (authManager.changePassword(player, args[0], args[1])) {
            player.sendMessage("§a密码修改成功");
        } else {
            player.sendMessage("§c旧密码错误");
        }
        return true;
    }
}
