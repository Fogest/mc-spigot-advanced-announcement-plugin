package com.advancedrotating.messages.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.advancedrotating.messages.AdvancedRotatingMessagesPlugin;
import com.advancedrotating.messages.models.MessageGroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final AdvancedRotatingMessagesPlugin plugin;
    private final File groupsFile;
    private FileConfiguration groupsConfig;

    public ConfigManager(AdvancedRotatingMessagesPlugin plugin) {
        this.plugin = plugin;
        this.groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!groupsFile.exists()) {
            createDefaultConfig();
        }
        groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
    }

    private void createDefaultConfig() {
        try {
            groupsFile.getParentFile().mkdirs();
            groupsFile.createNewFile();

            FileConfiguration config = YamlConfiguration.loadConfiguration(groupsFile);

            config.set("groups.announcements.name", "Announcements");
            config.set("groups.announcements.frequency", 5);
            config.set("groups.announcements.prefix", "&a[&lANNOUNCEMENT&r&a]&r ");
            config.set("groups.announcements.suffix", "");
            config.createSection("groups.announcements.messages");
            config.set("groups.announcements.messages.0", "Welcome to our server!");
            config.set("groups.announcements.messages.1", "Don't forget to read the rules!");
            config.set("groups.announcements.messages.2", "Join our Discord server for updates!");

            config.set("groups.tips.name", "Tips");
            config.set("groups.tips.frequency", 10);
            config.set("groups.tips.prefix", "&e[&lTIP&r&e]&r ");
            config.set("groups.tips.suffix", "");
            config.createSection("groups.tips.messages");
            config.set("groups.tips.messages.0", "Use /spawn to return to spawn!");
            config.set("groups.tips.messages.1", "You can use /home to teleport home!");
            config.set("groups.tips.messages.2", "Press F3 to see your coordinates!");

            config.save(groupsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default groups.yml: " + e.getMessage());
        }
    }

    public void saveConfig() {
        try {
            groupsConfig.save(groupsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save groups.yml: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
    }

    public Map<String, MessageGroup> getGroups() {
        Map<String, MessageGroup> groups = new HashMap<>();

        if (groupsConfig.getConfigurationSection("groups") == null) {
            return groups;
        }

        for (String groupId : groupsConfig.getConfigurationSection("groups").getKeys(false)) {
            String path = "groups." + groupId;

            String name = groupsConfig.getString(path + ".name", groupId);
            int frequency = groupsConfig.getInt(path + ".frequency", 5);
            String prefix = groupsConfig.getString(path + ".prefix", "");
            String suffix = groupsConfig.getString(path + ".suffix", "");

            List<String> messages = new ArrayList<>();
            if (groupsConfig.getConfigurationSection(path + ".messages") != null) {
                for (String messageKey : groupsConfig.getConfigurationSection(path + ".messages").getKeys(false)) {
                    messages.add(groupsConfig.getString(path + ".messages." + messageKey));
                }
            }

            MessageGroup group = new MessageGroup(groupId, name, frequency, prefix, suffix, messages);
            groups.put(groupId, group);
        }

        return groups;
    }

    public MessageGroup getGroup(String groupId) {
        return getGroups().get(groupId);
    }

    public void saveGroup(MessageGroup group) {
        String path = "groups." + group.getId();

        groupsConfig.set(path + ".name", group.getName());
        groupsConfig.set(path + ".frequency", group.getFrequency());
        groupsConfig.set(path + ".prefix", group.getPrefix());
        groupsConfig.set(path + ".suffix", group.getSuffix());

        groupsConfig.set(path + ".messages", null);
        for (int i = 0; i < group.getMessages().size(); i++) {
            groupsConfig.set(path + ".messages." + i, group.getMessages().get(i));
        }

        saveConfig();
    }

    public void deleteGroup(String groupId) {
        groupsConfig.set("groups." + groupId, null);
        saveConfig();
    }

    public boolean groupExists(String groupId) {
        return groupsConfig.contains("groups." + groupId);
    }
}