package top.chenray.qlogin.command;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * /changepassword <旧密码> <新密码> - 修改密码命令
 */
public class ChangePasswordCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("changepassword")
            .then(CommandManager.argument("oldPassword", StringArgumentType.word())
                .then(CommandManager.argument("newPassword", StringArgumentType.word())
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        ServerPlayerEntity player = source.getPlayer();
                        if (player == null) {
                            source.sendError(Text.literal("§c此命令只能由玩家执行"));
                            return 0;
                        }
                        String oldPassword = StringArgumentType.getString(context, "oldPassword");
                        String newPassword = StringArgumentType.getString(context, "newPassword");
                        return executeChangePassword(player, oldPassword, newPassword);
                    })
                )
            )
        );
    }

    private static int executeChangePassword(ServerPlayerEntity player, String oldPassword, String newPassword) {
        LoginManager loginManager = LoginManager.getInstance();
        LoginState state = loginManager.getState(player.getUuid());
        DatabaseManager db = DatabaseManager.getInstance();

        if (state != LoginState.LOGGED_IN) {
            TextUtils.sendWarning(player, "login.already");
            return 0;
        }

        if (oldPassword.equals(newPassword)) {
            TextUtils.sendError(player, "changepassword.same");
            return 0;
        }

        int minLen = ModConfig.getInstance().getPasswordMinLength();
        int maxLen = ModConfig.getInstance().getPasswordMaxLength();

        if (newPassword.length() < minLen) {
            TextUtils.sendError(player, "register.password_too_short", minLen);
            return 0;
        }

        if (newPassword.length() > maxLen) {
            TextUtils.sendError(player, "register.password_too_long", maxLen);
            return 0;
        }

        if (db.changePassword(player.getUuid(), oldPassword, newPassword)) {
            TextUtils.sendSuccess(player, "changepassword.success");
            return 1;
        } else {
            TextUtils.sendError(player, "changepassword.wrong");
            return 0;
        }
    }
}
