package top.chenray.qlogin.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * 标题消息辅助类 - 发送标题/副标题动画 (Mojang 映射版)
 */
public class TitleHelper {

    private static TitleHelper instance;

    private TitleHelper() {}

    public static synchronized TitleHelper getInstance() {
        if (instance == null) {
            instance = new TitleHelper();
        }
        return instance;
    }

    /**
     * 发送标题和副标题
     */
    public void sendTitle(ServerPlayer player, String titleText, String subtitleText) {
        Component title = Component.literal(titleText);
        Component subtitle = Component.literal(subtitleText);

        // 淡入 10t, 停留 70t, 淡出 20t
        sendTitle(player, title, subtitle, 10, 70, 20);
    }

    /**
     * 发送标题和副标题（含停留时间）
     */
    public void sendTitle(ServerPlayer player, Component title, Component subtitle,
                          int fadeIn, int stay, int fadeOut) {
        var animationPacket = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        var titlePacket = new ClientboundSetTitleTextPacket(title);
        var subtitlePacket = new ClientboundSetSubtitleTextPacket(subtitle);

        player.connection.send(animationPacket);
        player.connection.send(titlePacket);
        player.connection.send(subtitlePacket);
    }
}
