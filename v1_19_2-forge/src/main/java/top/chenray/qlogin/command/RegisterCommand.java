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

public class RegisterCommand {

    private static final org.slf4j.Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("register")
            .then(Commands.argument("password", StringArgumentType.word())
                .then(Commands.argument("confirmPassword", StringArgumentType.word())
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        ServerPlayer player = source.getPlayer();
                        if (player == null) {
                            source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                            return 0;
                        }
                        return executeRegister(player,
                            StringArgumentType.getString(context, "password"),
                            StringArgumentType.getString(context, "confirmPassword"));
                    })
                )
            )
        );
    }

    private static int executeRegister(ServerPlayer player, String password, String confirmPassword) {
        LoginManager lm = LoginManager.getInstance();
        DatabaseManager db = DatabaseManager.getInstance();

        if (lm.getState(player.getUUID()) == LoginState.LOGGED_IN) {
            TextUtils.sendWarning(player, "login.already"); return 0;
        }
        if (db.isPlayerRegistered(player.getUUID())) {
            TextUtils.sendMsg(player, "register.exists"); return 0;
        }
        if (!password.equals(confirmPassword)) {
            TextUtils.sendError(player, "register.password_mismatch"); return 0;
        }
        int min = ModConfig.getInstance().getPasswordMinLength();
        int max = ModConfig.getInstance().getPasswordMaxLength();
        if (password.length() < min) { TextUtils.sendError(player, "register.password_too_short", min); return 0; }
        if (password.length() > max) { TextUtils.sendError(player, "register.password_too_long", max); return 0; }

        if (db.registerPlayer(player.getUUID(), player.getName().getString(), password)) {
            lm.setLoggedIn(player.getUUID());
            lm.resetLoginFails(player.getUUID());
            db.updateLoginInfo(player.getUUID(), lm.getPlayerIp(player));
            TextUtils.sendSuccess(player, "register.success", player.getName().getString());
            TextUtils.sendTitle(player, "注册成功", "欢迎来到服务器！");
            LOGGER.info("Player {} registered", player.getName().getString());
            return 1;
        }
        TextUtils.sendError(player, "register.fail");
        return 0;
    }
}
