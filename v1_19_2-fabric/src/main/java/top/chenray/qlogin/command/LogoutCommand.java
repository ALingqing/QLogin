package top.chenray.qlogin.command;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class LogoutCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("logout")
            .executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player == null) { context.getSource().sendError(Text.literal("§c此命令只能由玩家执行")); return 0; }
                LoginManager lm = LoginManager.getInstance();
                if (lm.getState(player.getUuid()) != LoginState.LOGGED_IN) { TextUtils.sendWarning(player, "login.already"); return 0; }
                lm.setLoggedOut(player.getUuid());
                TextUtils.sendSuccess(player, "logout.success");
                return 1;
            })
        );
    }
}
