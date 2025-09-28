package com.advancedrotating.messages.scheduler;

import com.advancedrotating.messages.AdvancedRotatingMessagesPlugin;
import com.advancedrotating.messages.config.ConfigManager;
import com.advancedrotating.messages.database.DatabaseManager;
import com.advancedrotating.messages.database.DatabaseManager.QueuedMessage;
import com.advancedrotating.messages.models.MessageGroup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MessageScheduler {

    private final AdvancedRotatingMessagesPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final Map<String, Long> lastSentTimes;
    private final Queue<QueuedMessage> messageQueue;
    private BukkitTask schedulerTask;
    private long lastMessageSentTime;

    public MessageScheduler(AdvancedRotatingMessagesPlugin plugin, ConfigManager configManager, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.lastSentTimes = new HashMap<>();
        this.messageQueue = new LinkedList<>();
        this.lastMessageSentTime = 0;
    }

    public void start() {
        initializeGroups();

        schedulerTask = new BukkitRunnable() {
            @Override
            public void run() {
                processScheduledMessages();
            }
        }.runTaskTimer(plugin, 0L, 20L * 60L);
    }

    public void stop() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }
    }

    private void initializeGroups() {
        Map<String, MessageGroup> groups = configManager.getGroups();
        for (MessageGroup group : groups.values()) {
            if (group.getMessages().isEmpty()) {
                continue;
            }

            databaseManager.initializeGroupPool(group.getId(), group.getMessages().size());

            List<Integer> availableMessages = databaseManager.getAvailableMessages(group.getId());
            if (availableMessages.isEmpty()) {
                databaseManager.resetGroupPool(group.getId());
            }
        }
    }

    private void processScheduledMessages() {
        // Skip processing if no players are online
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            plugin.getLogger().fine("Skipping message processing - no players online");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long currentMinute = currentTime / (60 * 1000);
        long lastMinute = lastMessageSentTime / (60 * 1000);

        processQueuedMessages(currentTime);

        if (currentMinute > lastMinute) {
            scheduleGroupMessages(currentTime);
        }
    }

    private void processQueuedMessages(long currentTime) {
        List<QueuedMessage> queuedMessages = databaseManager.getQueuedMessages(currentTime);

        if (!queuedMessages.isEmpty()) {
            QueuedMessage messageToSend = queuedMessages.get(0);

            MessageGroup group = configManager.getGroup(messageToSend.getGroupId());
            if (group != null && messageToSend.getMessageIndex() < group.getMessages().size()) {
                sendMessage(group, messageToSend.getMessageIndex());
                databaseManager.markQueuedMessageAsSent(messageToSend.getId());
                lastMessageSentTime = currentTime;
            }

            for (int i = 1; i < queuedMessages.size(); i++) {
                QueuedMessage queuedMsg = queuedMessages.get(i);
                long nextAvailableTime = currentTime + (60 * 1000 * i);
                databaseManager.addMessageToQueue(queuedMsg.getGroupId(), queuedMsg.getMessageIndex(), nextAvailableTime);
                databaseManager.markQueuedMessageAsSent(queuedMsg.getId());
            }
        }
    }

    private void scheduleGroupMessages(long currentTime) {
        Map<String, MessageGroup> groups = configManager.getGroups();
        List<MessageGroup> groupsToSend = new ArrayList<>();

        for (MessageGroup group : groups.values()) {
            if (group.getMessages().isEmpty()) {
                continue;
            }

            long lastSent = lastSentTimes.getOrDefault(group.getId(), 0L);
            long timeSinceLastSent = (currentTime - lastSent) / (60 * 1000);

            if (timeSinceLastSent >= group.getFrequency()) {
                groupsToSend.add(group);
            }
        }

        if (groupsToSend.isEmpty()) {
            return;
        }

        long currentMinute = currentTime / (60 * 1000);
        long lastMinute = lastMessageSentTime / (60 * 1000);

        if (groupsToSend.size() == 1 && currentMinute > lastMinute) {
            MessageGroup group = groupsToSend.get(0);
            sendRandomMessage(group, currentTime);
        } else if (groupsToSend.size() > 1) {
            for (int i = 0; i < groupsToSend.size(); i++) {
                MessageGroup group = groupsToSend.get(i);
                long scheduledTime = currentTime + (60 * 1000 * i);

                List<Integer> availableMessages = databaseManager.getAvailableMessages(group.getId());
                if (availableMessages.isEmpty()) {
                    databaseManager.resetGroupPool(group.getId());
                    availableMessages = databaseManager.getAvailableMessages(group.getId());
                }

                if (!availableMessages.isEmpty()) {
                    int randomIndex = ThreadLocalRandom.current().nextInt(availableMessages.size());
                    int messageIndex = availableMessages.get(randomIndex);

                    if (i == 0 && currentMinute > lastMinute) {
                        sendMessage(group, messageIndex);
                        databaseManager.markMessageAsSent(group.getId(), messageIndex);
                        lastMessageSentTime = currentTime;
                    } else {
                        databaseManager.addMessageToQueue(group.getId(), messageIndex, scheduledTime);
                        databaseManager.markMessageAsSent(group.getId(), messageIndex);
                    }

                    lastSentTimes.put(group.getId(), currentTime);
                }
            }
        }
    }

    private void sendRandomMessage(MessageGroup group, long currentTime) {
        List<Integer> availableMessages = databaseManager.getAvailableMessages(group.getId());

        if (availableMessages.isEmpty()) {
            databaseManager.resetGroupPool(group.getId());
            availableMessages = databaseManager.getAvailableMessages(group.getId());
        }

        if (availableMessages.isEmpty()) {
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(availableMessages.size());
        int messageIndex = availableMessages.get(randomIndex);

        sendMessage(group, messageIndex);
        databaseManager.markMessageAsSent(group.getId(), messageIndex);
        lastSentTimes.put(group.getId(), currentTime);
        lastMessageSentTime = currentTime;
    }

    public void sendMessage(MessageGroup group, int messageIndex) {
        if (messageIndex < 0 || messageIndex >= group.getMessages().size()) {
            return;
        }

        String message = group.getMessages().get(messageIndex);
        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
            group.getPrefix() + message + group.getSuffix());

        Bukkit.broadcastMessage(formattedMessage);
    }

    public void forceSendMessage(String groupId, int messageIndex) {
        MessageGroup group = configManager.getGroup(groupId);
        if (group == null || messageIndex < 0 || messageIndex >= group.getMessages().size()) {
            return;
        }

        sendMessage(group, messageIndex);

        List<Integer> availableMessages = databaseManager.getAvailableMessages(groupId);
        if (availableMessages.contains(messageIndex)) {
            databaseManager.markMessageAsSent(groupId, messageIndex);
        }
    }

    public void reloadScheduler() {
        lastSentTimes.clear();
        messageQueue.clear();
        initializeGroups();
    }
}