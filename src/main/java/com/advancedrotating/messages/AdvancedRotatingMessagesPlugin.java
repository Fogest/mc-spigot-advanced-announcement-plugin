package com.advancedrotating.messages;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.advancedrotating.messages.config.ConfigManager;
import com.advancedrotating.messages.database.DatabaseManager;
import com.advancedrotating.messages.commands.ARMCommand;
import com.advancedrotating.messages.scheduler.MessageScheduler;
import com.advancedrotating.messages.managers.GroupManager;

import java.io.File;

public class AdvancedRotatingMessagesPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private MessageScheduler messageScheduler;
    private GroupManager groupManager;

    @Override
    public void onEnable() {
        getLogger().info("AdvancedRotatingMessages is starting up...");

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        configManager = new ConfigManager(this);

        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        groupManager = new GroupManager(this, configManager, databaseManager);
        groupManager.initializeAllGroups();

        messageScheduler = new MessageScheduler(this, configManager, databaseManager);
        messageScheduler.start();

        getCommand("arm").setExecutor(new ARMCommand(this, configManager, databaseManager));

        getLogger().info("AdvancedRotatingMessages has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AdvancedRotatingMessages is shutting down...");

        if (messageScheduler != null) {
            messageScheduler.stop();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("AdvancedRotatingMessages has been disabled successfully!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MessageScheduler getMessageScheduler() {
        return messageScheduler;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }
}