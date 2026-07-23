package top.chenray.qlogin.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 文本工具类 - Forge 1.19.2 (Mojang 官方映射)
 */
public class TextUtils {

    public static String t(String key, Object... args) { return LanguageManager.tr(key, args); }

    public static MutableComponent prefixed(Component text) {
        return Component.literal(LanguageManager.tr("prefix")).append(text);
    }

    public static void sendMessage(ServerPlayer player, Component message) { player.sendSystemMessage(message); }

    public static void sendMsg(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(prefixed(Component.literal(LanguageManager.tr(key, args))));
    }

    public static void sendError(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(prefixed(Component.literal(LanguageManager.tr(key, args))));
    }

    public static void sendSuccess(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(prefixed(Component.literal(LanguageManager.tr(key, args))));
    }

    public static void sendWarning(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(prefixed(Component.literal(LanguageManager.tr(key, args))));
    }

    public static void sendActionBar(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.literal(LanguageManager.tr(key, args)), true);
    }

    public static void sendTitle(ServerPlayer player, String title, String subtitle) {
        player.displayClientMessage(Component.literal("§6" + title + " §e" + subtitle), false);
    }

    public static MutableComponent success(String text) { return Component.literal("§a" + text); }
    public static MutableComponent error(String text) { return Component.literal("§c" + text); }
    public static MutableComponent warning(String text) { return Component.literal("§e" + text); }
    public static MutableComponent info(String text) { return Component.literal("§b" + text); }
}
