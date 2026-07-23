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

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void onExecute(ParseResults<ServerCommandSource> parse, String command, CallbackInfo ci) {
        var source = parse.getContext().getSource();
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            LoginManager lm = LoginManager.getInstance();
            if (lm.isLoggedIn(player.getUuid())) return;

            String cmd = command.startsWith("/") ? command.substring(1) : command;
            String name = cmd.split(" ")[0].toLowerCase();
            if (name.equals("register") || name.equals("reg") || name.equals("login") || name.equals("l") || name.equals("log") || name.equals("loginmod")) return;

            player.sendMessage(Text.literal("§7[§b登录系统§7] §c✘ 请先登录后再执行命令！"));
            ci.cancel();
        }
    }
}
