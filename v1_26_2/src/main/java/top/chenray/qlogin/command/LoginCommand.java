package top.chenray.qlogin.command;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.util.LanguageManager;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /login <密码> - 登录命令 (Mojang 映射版)
 */
public class LoginCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                 CommandBuildContext registryAccess,
                                 Commands.CommandSelection environment) {
        // /login <密码>
        dispatcher.register(Commands.literal("login")
            .then(Commands.argument("password", StringArgumentType.word())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayer();
                    if (player == null) {
                        source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                        return 0;
                    }
                    String password = StringArgumentType.getString(context, "password");
                    return executeLogin(player, password);
                })
            )
        );

        // /l <密码> - 快捷登录
        dispatcher.register(Commands.literal("l")
            .then(Commands.argument("password", StringArgumentType.word())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayer();
                    if (player == null) {
                        source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                        return 0;
                    }
                    String password = StringArgumentType.getString(context, "password");
                    return executeLogin(player, password);
                })
            )
        );
    }

    private static int executeLogin(ServerPlayer player, String password) {
        LoginManager loginManager = LoginManager.getInstance();
        LoginState state = loginManager.getState(player.getUUID());
        DatabaseManager db = DatabaseManager.getInstance();

        // 已经登录
        if (state == LoginState.LOGGED_IN) {
            TextUtils.sendMsg(player, "login.already");
            return 1;
        }

        // 未注册
        if (state == LoginState.UNREGISTERED) {
            TextUtils.sendError(player, "login.unregistered");
            return 0;
        }

        // 验证密码
        if (!db.verifyPassword(player.getUUID(), password)) {
            loginManager.recordLoginFail(player.getUUID());

            // 检查是否触发 IP 封禁
            if (loginManager.isIpBanned(loginManager.getPlayerIp(player))) {
                player.connection.disconnect(Component.literal(LanguageManager.tr("ban.too_many_attempts")));
            }

            int maxAttempts = ModConfig.getInstance().getMaxLoginAttempts();
            int remaining = maxAttempts - loginManager.getIpFailCount(loginManager.getPlayerIp(player));
            TextUtils.sendError(player, "login.fail", Math.max(0, remaining));
            return 0;
        }

        // 登录成功
        loginManager.setLoggedIn(player.getUUID());
        loginManager.resetLoginFails(player.getUUID());

        String ip = loginManager.getPlayerIp(player);
        db.updateLoginInfo(player.getUUID(), ip);

        TextUtils.sendSuccess(player, "login.success", player.getScoreboardName());
        top.chenray.qlogin.LoginMod.LOGGER.info("玩家 {} 已登录", player.getScoreboardName());
        return 1;
    }
}
