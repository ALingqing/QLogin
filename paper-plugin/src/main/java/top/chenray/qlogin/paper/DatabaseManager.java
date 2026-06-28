package top.chenray.qlogin.paper;

import java.io.File;
import java.sql.*;

public class DatabaseManager {

    private final QLoginPaper plugin;
    private Connection connection;

    public DatabaseManager(QLoginPaper plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(plugin.getDataFolder(), "qlogin.db");
            plugin.getDataFolder().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
            plugin.getLogger().info("数据库已连接: " + dbFile.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "last_login BIGINT DEFAULT 0," +
                "created_at BIGINT NOT NULL" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 注册玩家
     */
    public boolean registerPlayer(String uuid, String name, String passwordHash) {
        String sql = "INSERT OR REPLACE INTO players (uuid, name, password_hash, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, name);
            ps.setString(3, passwordHash);
            ps.setLong(4, System.currentTimeMillis());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().warning("注册失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取玩家密码哈希
     */
    public String getPasswordHash(String uuid) {
        String sql = "SELECT password_hash FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("查询密码失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 更新密码
     */
    public boolean updatePassword(String uuid, String newPasswordHash) {
        String sql = "UPDATE players SET password_hash = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().warning("更新密码失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLogin(String uuid) {
        String sql = "UPDATE players SET last_login = ?, name = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(uuid)).getName());
            ps.setString(3, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            // ignore
        }
    }

    /**
     * 检查玩家是否已注册
     */
    public boolean isRegistered(String uuid) {
        return getPasswordHash(uuid) != null;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }
}
