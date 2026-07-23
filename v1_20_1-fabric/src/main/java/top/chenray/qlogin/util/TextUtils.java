package top.chenray.qlogin.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * 文本工具类 - 提供多语言彩色格式化输出 (Yarn 映射版)
 */
public class TextUtils {

    public static String t(String key, Object... args) {
        return LanguageManager.tr(key, args);
    }

    public static MutableText prefixed(Text text) {
        return Text.literal(LanguageManager.tr("prefix")).append(text);
    }

    public static void sendMessage(ServerPlayerEntity player, Text message) {
        player.sendMessage(message);
    }

    public static void sendMsg(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(prefixed(Text.literal(LanguageManager.tr(key, args))));
    }

    public static void sendError(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(prefixed(Text.literal(LanguageManager.tr(key, args))));
    }

    public static void sendSuccess(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(prefixed(Text.literal(LanguageManager.tr(key, args))));
    }

    public static void sendWarning(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(prefixed(Text.literal(LanguageManager.tr(key, args))));
    }

    public static void sendActionBar(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(Text.literal(LanguageManager.tr(key, args)), true);
    }

    public static void sendTitle(ServerPlayerEntity player, String title, String subtitle) {
        TitleHelper.getInstance().sendTitle(player, title, subtitle);
    }

    public static MutableText success(String text) {
        return Text.literal("§a" + text);
    }

    public static MutableText error(String text) {
        return Text.literal("§c" + text);
    }

    public static MutableText warning(String text) {
        return Text.literal("§e" + text);
    }

    public static MutableText info(String text) {
        return Text.literal("§b" + text);
    }
}
