package top.chenray.qlogin.command;

import top.chenray.qlogin.config.ModConfig;
import top.chenray.qlogin.database.DatabaseManager;
import top.chenray.qlogin.util.LanguageManager;
import top.chenray.qlogin.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdminCommand {
    private static final org.slf4j.Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("qlogin")
            .requires(source -> source.hasPermission(3))
            .then(Commands.literal("reload")
                .executes(context -> {
                    ModConfig.load(context.getSource().getServer().getServerDirectory().toPath().resolve("config").resolve("loginmod"));
                    LanguageManager.reload();
                    TextUtils.sendMsg(context.getSource().getPlayer(), "admin.reload");
                    return 1;
                })
            )
            .then(Commands.literal("resetpassword")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("newPassword", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                            String newPw = StringArgumentType.getString(context, "newPassword");
                            if (!DatabaseManager.getInstance().isPlayerRegistered(target.getUUID())) {
                                context.getSource().sendFailure(Component.literal("§c该玩家尚未注册")); return 0;
                            }
                            if (DatabaseManager.getInstance().adminResetPassword(target.getUUID(), newPw)) {
                                TextUtils.sendMsg(context.getSource(), "admin.reset_password", target.getName().getString());
                                return 1;
                            }
                            context.getSource().sendFailure(Component.literal("§c重置密码失败"));
                            return 0;
                        })
                    )
                )
            )
        );
    }
}
