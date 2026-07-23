package top.chenray.qlogin.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import top.chenray.qlogin.LoginMod;
import org.slf4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance;
    private static Path configPath;

    @SerializedName("login_timeout_seconds") private int loginTimeoutSeconds = 60;
    @SerializedName("max_login_attempts") private int maxLoginAttempts = 5;
    @SerializedName("ban_duration_seconds") private int banDurationSeconds = 300;
    @SerializedName("kick_on_timeout") private boolean kickOnTimeout = true;
    @SerializedName("allow_spectator_on_timeout") private boolean allowSpectatorOnTimeout = false;
    @SerializedName("password_min_length") private int passwordMinLength = 4;
    @SerializedName("password_max_length") private int passwordMaxLength = 32;
    @SerializedName("language") private String language = "zh_cn";

    public int getLoginTimeoutSeconds() { return loginTimeoutSeconds; }
    public int getMaxLoginAttempts() { return maxLoginAttempts; }
    public int getBanDurationSeconds() { return banDurationSeconds; }
    public boolean isKickOnTimeout() { return kickOnTimeout; }
    public int getPasswordMinLength() { return passwordMinLength; }
    public int getPasswordMaxLength() { return passwordMaxLength; }
    public String getLanguage() { return language; }

    public static ModConfig load(Path configDir) {
        configPath = configDir.resolve("loginmod.json");
        Logger logger = LoginMod.LOGGER;
        if (Files.exists(configPath)) {
            try {
                instance = GSON.fromJson(Files.readString(configPath), ModConfig.class);
                logger.info("配置已加载: {}", configPath);
            } catch (Exception e) {
                logger.error("加载配置失败", e);
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
            save();
        }
        return instance;
    }

    public static ModConfig getInstance() { return instance; }

    public static void save() {
        if (configPath == null || instance == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(instance));
        } catch (IOException e) {
            LoginMod.LOGGER.error("保存配置失败", e);
        }
    }
}
