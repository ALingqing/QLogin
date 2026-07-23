package top.chenray.qlogin.command;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ChangePasswordCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("changepassword")
            .then(Commands.argument("oldPassword", StringArgumentType.word())
                .then(Commands.argument("newPassword", StringArgumentType.word())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayer();
                        if (player == null) { context.getSource().sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }
                        return executeChangePassword(player,
                            StringArgumentType.getString(context, "oldPassword"),
                            StringArgumentType.getString(context, "newPassword"));
                    })
                )
            )
        );
    }

    private static int executeChangePassword(ServerPlayer player, String oldPw, String newPw) {
        LoginManager lm = LoginManager.getInstance();
        if (lm.getState(player.getUUID()) != LoginState.LOGGED_IN) { TextUtils.sendWarning(player, "login.already"); return 0; }
        if (oldPw.equals(newPw)) { TextUtils.sendError(player, "changepassword.same"); return 0; }
        int min = ModConfig.getInstance().getPasswordMinLength();
        int max = ModConfig.getInstance().getPasswordMaxLength();
        if (newPw.length() < min) { TextUtils.sendError(player, "register.password_too_short", min); return 0; }
        if (newPw.length() > max) { TextUtils.sendError(player, "register.password_too_long", max); return 0; }
        if (DatabaseManager.getInstance().changePassword(player.getUUID(), oldPw, newPw)) {
            TextUtils.sendSuccess(player, "changepassword.success"); return 1;
        }
        TextUtils.sendError(player, "changepassword.wrong"); return 0;
    }
}
