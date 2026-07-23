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

public class RegisterCommand {
    private static final org.slf4j.Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("register")
            .then(CommandManager.argument("password", StringArgumentType.word())
                .then(CommandManager.argument("confirmPassword", StringArgumentType.word())
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) { context.getSource().sendError(Text.literal("§c此命令只能由玩家执行")); return 0; }
                        return executeRegister(player,
                            StringArgumentType.getString(context, "password"),
                            StringArgumentType.getString(context, "confirmPassword"));
                    })
                )
            )
        );
    }

    private static int executeRegister(ServerPlayerEntity player, String password, String confirmPassword) {
        LoginManager lm = LoginManager.getInstance();
        DatabaseManager db = DatabaseManager.getInstance();

        if (lm.getState(player.getUuid()) == LoginState.LOGGED_IN) { TextUtils.sendWarning(player, "login.already"); return 0; }
        if (db.isPlayerRegistered(player.getUuid())) { TextUtils.sendMsg(player, "register.exists"); return 0; }
        if (!password.equals(confirmPassword)) { TextUtils.sendError(player, "register.password_mismatch"); return 0; }

        int min = ModConfig.getInstance().getPasswordMinLength();
        int max = ModConfig.getInstance().getPasswordMaxLength();
        if (password.length() < min) { TextUtils.sendError(player, "register.password_too_short", min); return 0; }
        if (password.length() > max) { TextUtils.sendError(player, "register.password_too_long", max); return 0; }

        if (db.registerPlayer(player.getUuid(), player.getName().getString(), password)) {
            lm.setLoggedIn(player.getUuid());
            lm.resetLoginFails(player.getUuid());
            db.updateLoginInfo(player.getUuid(), lm.getPlayerIp(player));
            TextUtils.sendSuccess(player, "register.success", player.getName().getString());
            TextUtils.sendTitle(player, "注册成功", "欢迎来到服务器！");
            LOGGER.info("Player {} registered", player.getName().getString());
            return 1;
        }
        TextUtils.sendError(player, "register.fail");
        return 0;
    }
}
