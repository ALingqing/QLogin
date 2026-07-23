package top.chenray.qlogin.database;

import top.chenray.qlogin.security.PasswordHasher;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private static final Logger LOGGER = top.chenray.qlogin.LoginMod.LOGGER;
    private static DatabaseManager instance;
    private static Path databaseDir;
    private Connection connection;

    private DatabaseManager() {}

    public static void init(Path dir) { databaseDir = dir; }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void connect() {
        try {
            java.nio.file.Files.createDirectories(databaseDir);
            String url = "jdbc:sqlite:" + databaseDir.resolve("loginmod.db").toString();
            connection = DriverManager.getConnection(url);
            createTables();
            LOGGER.info("数据库已连接: {}", url);
        } catch (Exception e) {
            LOGGER.error("数据库连接失败", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    password_hash TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    last_ip TEXT,
                    last_login TIMESTAMP,
                    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            LOGGER.error("关闭数据库失败", e);
        }
    }

    public boolean isPlayerRegistered(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean registerPlayer(UUID uuid, String username, String password) {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(password, salt);
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO players (uuid, username, password_hash, salt) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, hash);
            stmt.setString(4, salt);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error("注册玩家失败", e);
            return false;
        }
    }

    public boolean verifyPassword(UUID uuid, String password) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT password_hash, salt FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return PasswordHasher.verify(password, rs.getString("salt"), rs.getString("password_hash"));
            }
        } catch (SQLException e) {
            LOGGER.error("验证密码失败", e);
        }
        return false;
    }

    public boolean changePassword(UUID uuid, String oldPassword, String newPassword) {
        if (!verifyPassword(uuid, oldPassword)) return false;
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(newPassword, salt);
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE players SET password_hash = ?, salt = ? WHERE uuid = ?")) {
            stmt.setString(1, hash);
            stmt.setString(2, salt);
            stmt.setString(3, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error("修改密码失败", e);
            return false;
        }
    }

    public boolean adminResetPassword(UUID uuid, String newPassword) {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(newPassword, salt);
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE players SET password_hash = ?, salt = ? WHERE uuid = ?")) {
            stmt.setString(1, hash);
            stmt.setString(2, salt);
            stmt.setString(3, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error("重置密码失败", e);
            return false;
        }
    }

    public void updateLoginInfo(UUID uuid, String ip) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE players SET last_ip = ?, last_login = CURRENT_TIMESTAMP WHERE uuid = ?")) {
            stmt.setString(1, ip);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("更新登录信息失败", e);
        }
    }
}
