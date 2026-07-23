package top.chenray.qlogin.mixin;

import top.chenray.qlogin.LoginManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin - 拦截玩家交互行为 (1.20.1 Yarn)
 */
@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void onInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand,
                                  BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (!LoginManager.getInstance().isLoggedIn(player.getUuid())) {
            player.sendMessage(net.minecraft.text.Text.literal("§c⚠ 请先登录后再与方块交互！"), true);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void onInteractItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand,
                                 CallbackInfoReturnable<ActionResult> cir) {
        if (!LoginManager.getInstance().isLoggedIn(player.getUuid())) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
