package top.chenray.qlogin.mixin;

import top.chenray.qlogin.LoginManager;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin - 拦截命令执行 (1.20.1 Yarn)
 */
@Mixin(CommandManager.class)
public class CommandManagerMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void onExecute(ParseResults<ServerCommandSource> parse, String command,
                            CallbackInfo ci) {
        var source = parse.getContext().getSource();

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            LoginManager loginManager = LoginManager.getInstance();

            if (loginManager.isLoggedIn(player.getUuid())) return;

            String cmdText = command.startsWith("/") ? command.substring(1) : command;
            String cmdName = cmdText.split(" ")[0].toLowerCase();

            if (cmdName.equals("register") || cmdName.equals("reg") ||
                cmdName.equals("login") || cmdName.equals("l") || cmdName.equals("log") ||
                cmdName.equals("loginmod")) {
                return;
            }

            player.sendMessage(Text.literal("§7[§b登录系统§7] §c✘ 请先登录后再执行命令！"));
            ci.cancel();
        }
    }
}
