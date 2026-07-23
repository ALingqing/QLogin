package top.chenray.qlogin.command;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LoginCommand {
    private static final org.slf4j.Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("login")
            .then(Commands.argument("password", StringArgumentType.word())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) { context.getSource().sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }
                    return executeLogin(player, StringArgumentType.getString(context, "password"));
                })
            )
        );
        dispatcher.register(Commands.literal("l")
            .then(Commands.argument("password", StringArgumentType.word())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) { context.getSource().sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }
                    return executeLogin(player, StringArgumentType.getString(context, "password"));
                })
            )
        );
    }

    private static int executeLogin(ServerPlayer player, String password) {
        LoginManager lm = LoginManager.getInstance();
        DatabaseManager db = DatabaseManager.getInstance();
        if (lm.getState(player.getUUID()) == LoginState.LOGGED_IN) { TextUtils.sendWarning(player, "login.already"); return 0; }
        if (!db.isPlayerRegistered(player.getUUID())) { TextUtils.sendMsg(player, "register.exists"); return 0; }
        if (db.verifyPassword(player.getUUID(), password)) {
            lm.setLoggedIn(player.getUUID()); lm.resetLoginFails(player.getUUID());
            db.updateLoginInfo(player.getUUID(), lm.getPlayerIp(player));
            TextUtils.sendSuccess(player, "login.success", player.getDisplayName().getString());
            TextUtils.sendTitle(player, "登录成功", "欢迎回来！");
            LOGGER.info("Player {} logged in", player.getName().getString()); return 1;
        }
        lm.incrementLoginFails(player.getUUID());
        TextUtils.sendError(player, "login.fail"); return 0;
    }
}
