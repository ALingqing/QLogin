package top.chenray.qlogin.command;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /register <密码> <确认密码> - 注册命令 (Mojang 映射版)
 */
public class RegisterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                 CommandBuildContext registryAccess,
                                 Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("register")
            .then(Commands.argument("password", StringArgumentType.word())
                .then(Commands.argument("confirmPassword", StringArgumentType.word())
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        ServerPlayer player = source.getPlayer();
                        if (player == null) {
                            source.sendFailure(net.minecraft.network.chat.Component.literal("§c此命令只能由玩家执行"));
                            return 0;
                        }
                        String password = StringArgumentType.getString(context, "password");
                        String confirmPassword = StringArgumentType.getString(context, "confirmPassword");
                        return executeRegister(player, password, confirmPassword);
                    })
                )
            )
        );
    }

    private static int executeRegister(ServerPlayer player, String password, String confirmPassword) {
        LoginManager loginManager = LoginManager.getInstance();
        LoginState state = loginManager.getState(player.getUUID());
        DatabaseManager db = DatabaseManager.getInstance();

        // 已注册玩家不能再次注册
        if (state == LoginState.LOGGED_IN || state == LoginState.NOT_LOGGED_IN) {
            TextUtils.sendError(player, "register.exists");
            return 0;
        }

        // 验证密码长度
        ModConfig config = ModConfig.getInstance();
        if (password.length() < config.getPasswordMinLength() || password.length() > config.getPasswordMaxLength()) {
            TextUtils.sendError(player, "register.password_length",
                config.getPasswordMinLength(), config.getPasswordMaxLength());
            return 0;
        }

        // 验证两次密码一致
        if (!password.equals(confirmPassword)) {
            TextUtils.sendError(player, "register.password_mismatch");
            return 0;
        }

        // 执行注册
        String ip = LoginManager.getInstance().getPlayerIp(player);
        if (db.registerPlayer(player.getUUID(), player.getScoreboardName(), password, ip)) {
            loginManager.setLoggedIn(player.getUUID());
            loginManager.resetLoginFails(player.getUUID());
            TextUtils.sendSuccess(player, "register.success", player.getScoreboardName());
            top.chenray.qlogin.LoginMod.LOGGER.info("玩家 {} 已注册", player.getScoreboardName());
            return 1;
        } else {
            TextUtils.sendError(player, "register.fail");
            return 0;
        }
    }
}
