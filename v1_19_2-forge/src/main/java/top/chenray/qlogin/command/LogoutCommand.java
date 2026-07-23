package top.chenray.qlogin.command;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LogoutCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("logout")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayer();
                if (player == null) {
                    context.getSource().sendFailure(Component.literal("§c此命令只能由玩家执行"));
                    return 0;
                }
                LoginManager lm = LoginManager.getInstance();
                if (lm.getState(player.getUUID()) != LoginState.LOGGED_IN) {
                    TextUtils.sendWarning(player, "login.already"); return 0;
                }
                lm.setLoggedOut(player.getUUID());
                TextUtils.sendSuccess(player, "logout.success");
                return 1;
            })
        );
    }
}
