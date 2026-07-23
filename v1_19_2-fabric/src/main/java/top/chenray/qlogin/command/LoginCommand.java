package top.chenray.qlogin.command;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.LoginState;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class LoginCommand {
    private static final org.slf4j.Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("login")
            .then(CommandManager.argument("password", StringArgumentType.word())
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) { context.getSource().sendError(Text.literal("§c此命令只能由玩家执行")); return 0; }
                    return executeLogin(player, StringArgumentType.getString(context, "password"));
                })
            )
        );
        dispatcher.register(CommandManager.literal("l")
            .then(CommandManager.argument("password", StringArgumentType.word())
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) { context.getSource().sendError(Text.literal("§c此命令只能由玩家执行")); return 0; }
                    return executeLogin(player, StringArgumentType.getString(context, "password"));
                })
            )
        );
    }

    private static int executeLogin(ServerPlayerEntity player, String password) {
        LoginManager lm = LoginManager.getInstance();
        DatabaseManager db = DatabaseManager.getInstance();

        if (lm.getState(player.getUuid()) == LoginState.LOGGED_IN) { TextUtils.sendWarning(player, "login.already"); return 0; }
        if (!db.isPlayerRegistered(player.getUuid())) { TextUtils.sendMsg(player, "register.exists"); return 0; }

        if (db.verifyPassword(player.getUuid(), password)) {
            lm.setLoggedIn(player.getUuid());
            lm.resetLoginFails(player.getUuid());
            db.updateLoginInfo(player.getUuid(), lm.getPlayerIp(player));
            TextUtils.sendSuccess(player, "login.success", player.getName().getString());
            TextUtils.sendTitle(player, "登录成功", "欢迎回来！");
            LOGGER.info("Player {} logged in", player.getName().getString());
            return 1;
        }
        lm.incrementLoginFails(player.getUuid());
        TextUtils.sendError(player, "login.fail");
        return 0;
    }
}
