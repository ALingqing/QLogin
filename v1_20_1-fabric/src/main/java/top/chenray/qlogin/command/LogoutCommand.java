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

/**
 * /logout - 退出登录命令
 */
public class LogoutCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("logout")
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                ServerPlayerEntity player = source.getPlayer();
                if (player == null) {
                    source.sendError(Text.literal("§c此命令只能由玩家执行"));
                    return 0;
                }

                LoginManager loginManager = LoginManager.getInstance();
                LoginState state = loginManager.getState(player.getUuid());

                if (state != LoginState.LOGGED_IN) {
                    TextUtils.sendWarning(player, "login.already");
                    return 0;
                }

                loginManager.setLoggedOut(player.getUuid());
                TextUtils.sendSuccess(player, "logout.success");

                return 1;
            })
        );
    }
}
