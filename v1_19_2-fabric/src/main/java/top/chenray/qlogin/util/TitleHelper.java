package top.chenray.qlogin.util;

import net.minecraft.server.network.ServerPlayerEntity;

public interface TitleHelper {

    static TitleHelper getInstance() { return Holder.INSTANCE; }
    static void setInstance(TitleHelper instance) { Holder.INSTANCE = instance; }
    void sendTitle(ServerPlayerEntity player, String title, String subtitle);

    class Holder {
        private static TitleHelper INSTANCE = new DefaultTitleHelper();
    }

    class DefaultTitleHelper implements TitleHelper {
        @Override
        public void sendTitle(ServerPlayerEntity player, String title, String subtitle) {
            player.sendMessage(
                net.minecraft.text.Text.literal("§6" + title + " §e" + subtitle), true);
        }
    }
}
