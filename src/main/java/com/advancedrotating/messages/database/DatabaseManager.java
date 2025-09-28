package com.advancedrotating.messages.database;

import com.advancedrotating.messages.AdvancedRotatingMessagesPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private final AdvancedRotatingMessagesPlugin plugin;
    private Connection connection;
    private final String dbPath;

    public DatabaseManager(AdvancedRotatingMessagesPlugin plugin) {
        this.plugin = plugin;
        this.dbPath = new File(plugin.getDataFolder(), "messages.db").getAbsolutePath();
    }

    public void initialize() {
        try {
            connect();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    private void createTables() throws SQLException {
        String createMessagePoolTable = """
            CREATE TABLE IF NOT EXISTS message_pool (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                group_id TEXT NOT NULL,
                message_index INTEGER NOT NULL,
                is_sent BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(group_id, message_index)
            )
        """;

        String createMessageQueueTable = """
            CREATE TABLE IF NOT EXISTS message_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                group_id TEXT NOT NULL,
                message_index INTEGER NOT NULL,
                scheduled_time TIMESTAMP NOT NULL,
                is_sent BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (PreparedStatement stmt1 = connection.prepareStatement(createMessagePoolTable);
             PreparedStatement stmt2 = connection.prepareStatement(createMessageQueueTable)) {
            stmt1.execute();
            stmt2.execute();
        }
    }

    public void initializeGroupPool(String groupId, int messageCount) {
        try {
            clearGroupPool(groupId);

            String sql = "INSERT OR IGNORE INTO message_pool (group_id, message_index, is_sent) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < messageCount; i++) {
                    stmt.setString(1, groupId);
                    stmt.setInt(2, i);
                    stmt.setBoolean(3, false);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize group pool for " + groupId + ": " + e.getMessage());
        }
    }

    public List<Integer> getAvailableMessages(String groupId) {
        List<Integer> availableMessages = new ArrayList<>();
        String sql = "SELECT message_index FROM message_pool WHERE group_id = ? AND is_sent = FALSE";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                availableMessages.add(rs.getInt("message_index"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get available messages for " + groupId + ": " + e.getMessage());
        }

        return availableMessages;
    }

    public void markMessageAsSent(String groupId, int messageIndex) {
        String sql = "UPDATE message_pool SET is_sent = TRUE WHERE group_id = ? AND message_index = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.setInt(2, messageIndex);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to mark message as sent for " + groupId + ": " + e.getMessage());
        }
    }

    public void resetGroupPool(String groupId) {
        String sql = "UPDATE message_pool SET is_sent = FALSE WHERE group_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to reset group pool for " + groupId + ": " + e.getMessage());
        }
    }

    public void clearGroupPool(String groupId) {
        String sql = "DELETE FROM message_pool WHERE group_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to clear group pool for " + groupId + ": " + e.getMessage());
        }
    }

    public void addMessageToQueue(String groupId, int messageIndex, long scheduledTime) {
        String sql = "INSERT INTO message_queue (group_id, message_index, scheduled_time) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.setInt(2, messageIndex);
            stmt.setTimestamp(3, new Timestamp(scheduledTime));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add message to queue: " + e.getMessage());
        }
    }

    public List<QueuedMessage> getQueuedMessages(long currentTime) {
        List<QueuedMessage> queuedMessages = new ArrayList<>();
        String sql = "SELECT * FROM message_queue WHERE scheduled_time <= ? AND is_sent = FALSE ORDER BY scheduled_time ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(currentTime));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                QueuedMessage queuedMessage = new QueuedMessage(
                    rs.getInt("id"),
                    rs.getString("group_id"),
                    rs.getInt("message_index"),
                    rs.getTimestamp("scheduled_time").getTime()
                );
                queuedMessages.add(queuedMessage);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get queued messages: " + e.getMessage());
        }

        return queuedMessages;
    }

    public void markQueuedMessageAsSent(int queueId) {
        String sql = "UPDATE message_queue SET is_sent = TRUE WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, queueId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to mark queued message as sent: " + e.getMessage());
        }
    }

    public void addNewMessageToPool(String groupId, int messageIndex) {
        String sql = "INSERT OR IGNORE INTO message_pool (group_id, message_index, is_sent) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.setInt(2, messageIndex);
            stmt.setBoolean(3, false);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add new message to pool: " + e.getMessage());
        }
    }

    public void removeMessageFromPool(String groupId, int messageIndex) {
        String sql = "DELETE FROM message_pool WHERE group_id = ? AND message_index = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.setInt(2, messageIndex);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove message from pool: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }

    public static class QueuedMessage {
        private final int id;
        private final String groupId;
        private final int messageIndex;
        private final long scheduledTime;

        public QueuedMessage(int id, String groupId, int messageIndex, long scheduledTime) {
            this.id = id;
            this.groupId = groupId;
            this.messageIndex = messageIndex;
            this.scheduledTime = scheduledTime;
        }

        public int getId() { return id; }
        public String getGroupId() { return groupId; }
        public int getMessageIndex() { return messageIndex; }
        public long getScheduledTime() { return scheduledTime; }
    }
}