package com.advancedrotating.messages.managers;

import com.advancedrotating.messages.AdvancedRotatingMessagesPlugin;
import com.advancedrotating.messages.config.ConfigManager;
import com.advancedrotating.messages.database.DatabaseManager;
import com.advancedrotating.messages.models.MessageGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupManager {

    private final AdvancedRotatingMessagesPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;

    public GroupManager(AdvancedRotatingMessagesPlugin plugin, ConfigManager configManager, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
    }

    public boolean createGroup(String groupId, String name, int frequency, String prefix, String suffix) {
        if (configManager.groupExists(groupId)) {
            return false;
        }

        MessageGroup group = new MessageGroup(groupId, name, frequency, prefix, suffix, new ArrayList<>());
        configManager.saveGroup(group);
        databaseManager.initializeGroupPool(groupId, 0);

        return true;
    }

    public boolean updateGroup(String groupId, String name, int frequency, String prefix, String suffix) {
        MessageGroup group = configManager.getGroup(groupId);
        if (group == null) {
            return false;
        }

        group.setName(name);
        group.setFrequency(frequency);
        group.setPrefix(prefix);
        group.setSuffix(suffix);

        configManager.saveGroup(group);
        return true;
    }

    public boolean deleteGroup(String groupId) {
        if (!configManager.groupExists(groupId)) {
            return false;
        }

        configManager.deleteGroup(groupId);
        databaseManager.clearGroupPool(groupId);
        return true;
    }

    public MessageGroup getGroup(String groupId) {
        return configManager.getGroup(groupId);
    }

    public Map<String, MessageGroup> getAllGroups() {
        return configManager.getGroups();
    }

    public boolean addMessage(String groupId, String message) {
        MessageGroup group = configManager.getGroup(groupId);
        if (group == null) {
            return false;
        }

        int newMessageIndex = group.getMessages().size();
        group.addMessage(message);
        configManager.saveGroup(group);

        databaseManager.addNewMessageToPool(groupId, newMessageIndex);
        return true;
    }

    public boolean updateMessage(String groupId, int messageIndex, String newMessage) {
        MessageGroup group = configManager.getGroup(groupId);
        if (group == null || messageIndex < 0 || messageIndex >= group.getMessages().size()) {
            return false;
        }

        group.updateMessage(messageIndex, newMessage);
        configManager.saveGroup(group);
        return true;
    }

    public boolean deleteMessage(String groupId, int messageIndex) {
        MessageGroup group = configManager.getGroup(groupId);
        if (group == null || messageIndex < 0 || messageIndex >= group.getMessages().size()) {
            return false;
        }

        group.removeMessage(messageIndex);

        for (int i = messageIndex; i < group.getMessages().size(); i++) {
            databaseManager.removeMessageFromPool(groupId, i + 1);
            databaseManager.addNewMessageToPool(groupId, i);
        }
        databaseManager.removeMessageFromPool(groupId, group.getMessages().size());

        configManager.saveGroup(group);
        return true;
    }

    public void initializeAllGroups() {
        Map<String, MessageGroup> groups = getAllGroups();
        for (MessageGroup group : groups.values()) {
            databaseManager.initializeGroupPool(group.getId(), group.getMessages().size());
        }
    }

    public List<String> getMessagesPage(String groupId, int page, int pageSize) {
        MessageGroup group = configManager.getGroup(groupId);
        if (group == null) {
            return new ArrayList<>();
        }

        List<String> allMessages = group.getMessages();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allMessages.size());

        if (startIndex >= allMessages.size()) {
            return new ArrayList<>();
        }

        return allMessages.subList(startIndex, endIndex);
    }

    public int getTotalPages(String groupId, int pageSize) {
        MessageGroup group = configManager.getGroup(groupId);
        if (group == null) {
            return 0;
        }

        return (int) Math.ceil((double) group.getMessages().size() / pageSize);
    }
}