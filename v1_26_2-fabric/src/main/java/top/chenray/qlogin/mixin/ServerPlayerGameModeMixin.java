package top.chenray.qlogin.mixin;

import top.chenray.qlogin.LoginManager;
import top.chenray.qlogin.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 混入玩家交互模式 - 拦截非登录状态下的方块/物品交互 (Mojang 映射版)
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    /**
     * 拦截右键点击方块（放置/交互）
     */
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onUseItemOn(ServerPlayer player, Level level, ItemStack stack,
                              InteractionHand hand, BlockHitResult hitResult,
                              CallbackInfoReturnable<InteractionResult> cir) {
        if (!LoginManager.getInstance().isLoggedIn(player.getUUID())) {
            player.sendSystemMessage(Component.literal(TextUtils.t("interact.blocked")));
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    /**
     * 拦截右键使用物品
     */
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void onUseItem(ServerPlayer player, Level level, ItemStack stack,
                            InteractionHand hand,
                            CallbackInfoReturnable<InteractionResult> cir) {
        if (!LoginManager.getInstance().isLoggedIn(player.getUUID())) {
            player.sendSystemMessage(Component.literal(TextUtils.t("interact.blocked")));
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
