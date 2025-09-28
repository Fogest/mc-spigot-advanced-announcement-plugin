package com.advancedrotating.messages.commands;

import com.advancedrotating.messages.AdvancedRotatingMessagesPlugin;
import com.advancedrotating.messages.config.ConfigManager;
import com.advancedrotating.messages.database.DatabaseManager;
import com.advancedrotating.messages.managers.GroupManager;
import com.advancedrotating.messages.models.MessageGroup;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ARMCommand implements CommandExecutor, TabCompleter {

    private final AdvancedRotatingMessagesPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final GroupManager groupManager;

    public ARMCommand(AdvancedRotatingMessagesPlugin plugin, ConfigManager configManager, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.groupManager = new GroupManager(plugin, configManager, databaseManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;
            case "list":
                handleListGroups(sender);
                break;
            case "info":
                handleGroupInfo(sender, args);
                break;
            case "create":
                handleCreateGroup(sender, args);
                break;
            case "edit":
                handleEditGroup(sender, args);
                break;
            case "delete":
                handleDeleteGroup(sender, args);
                break;
            case "messages":
                handleMessagesCommand(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /arm help for available commands.");
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Advanced Rotating Messages Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/arm list " + ChatColor.WHITE + "- List all message groups");
        sender.sendMessage(ChatColor.YELLOW + "/arm info <group> " + ChatColor.WHITE + "- View group information");
        sender.sendMessage(ChatColor.YELLOW + "/arm create <groupId> <name> <frequency> [prefix] [suffix] " + ChatColor.WHITE + "- Create new group");
        sender.sendMessage(ChatColor.YELLOW + "/arm edit <groupId> <name> <frequency> [prefix] [suffix] " + ChatColor.WHITE + "- Edit existing group");
        sender.sendMessage(ChatColor.YELLOW + "/arm delete <groupId> " + ChatColor.WHITE + "- Delete a group");
        sender.sendMessage(ChatColor.YELLOW + "/arm messages <group> list [page] " + ChatColor.WHITE + "- List messages in group");
        sender.sendMessage(ChatColor.YELLOW + "/arm messages <group> add <message> " + ChatColor.WHITE + "- Add message to group");
        sender.sendMessage(ChatColor.YELLOW + "/arm messages <group> edit <index> <message> " + ChatColor.WHITE + "- Edit message in group");
        sender.sendMessage(ChatColor.YELLOW + "/arm messages <group> delete <index> " + ChatColor.WHITE + "- Delete message from group");
        sender.sendMessage(ChatColor.YELLOW + "/arm messages <group> force <index> " + ChatColor.WHITE + "- Force send a message");
        sender.sendMessage(ChatColor.YELLOW + "/arm reload " + ChatColor.WHITE + "- Reload configuration");
    }

    private void handleListGroups(CommandSender sender) {
        if (!sender.hasPermission("advancedrotating.group.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to list groups.");
            return;
        }

        Map<String, MessageGroup> groups = groupManager.getAllGroups();

        if (groups.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No message groups configured.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Message Groups ===");
        for (MessageGroup group : groups.values()) {
            sender.sendMessage(ChatColor.YELLOW + group.getId() + " " + ChatColor.WHITE + "(" + group.getName() + ") - " +
                ChatColor.GRAY + group.getMessageCount() + " messages, " + group.getFrequency() + " min frequency");
        }
    }

    private void handleGroupInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("advancedrotating.group.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to view group information.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm info <groupId>");
            return;
        }

        String groupId = args[1];
        MessageGroup group = groupManager.getGroup(groupId);

        if (group == null) {
            sender.sendMessage(ChatColor.RED + "Group '" + groupId + "' not found.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Group Information: " + group.getId() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + group.getName());
        sender.sendMessage(ChatColor.YELLOW + "Frequency: " + ChatColor.WHITE + group.getFrequency() + " minutes");
        sender.sendMessage(ChatColor.YELLOW + "Prefix: " + ChatColor.WHITE + group.getPrefix());
        sender.sendMessage(ChatColor.YELLOW + "Suffix: " + ChatColor.WHITE + group.getSuffix());
        sender.sendMessage(ChatColor.YELLOW + "Messages: " + ChatColor.WHITE + group.getMessageCount());
    }

    private void handleCreateGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("advancedrotating.group.create")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to create groups.");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm create <groupId> <name> <frequency> [prefix] [suffix]");
            return;
        }

        String groupId = args[1];
        String name = args[2];
        int frequency;

        try {
            frequency = Integer.parseInt(args[3]);
            if (frequency <= 0) {
                sender.sendMessage(ChatColor.RED + "Frequency must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid frequency. Must be a number.");
            return;
        }

        String prefix = args.length > 4 ? args[4] : "";
        String suffix = args.length > 5 ? args[5] : "";

        if (groupManager.createGroup(groupId, name, frequency, prefix, suffix)) {
            sender.sendMessage(ChatColor.GREEN + "Group '" + groupId + "' created successfully.");
        } else {
            sender.sendMessage(ChatColor.RED + "Group '" + groupId + "' already exists.");
        }
    }

    private void handleEditGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("advancedrotating.group.edit")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to edit groups.");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm edit <groupId> <name> <frequency> [prefix] [suffix]");
            return;
        }

        String groupId = args[1];
        String name = args[2];
        int frequency;

        try {
            frequency = Integer.parseInt(args[3]);
            if (frequency <= 0) {
                sender.sendMessage(ChatColor.RED + "Frequency must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid frequency. Must be a number.");
            return;
        }

        String prefix = args.length > 4 ? args[4] : "";
        String suffix = args.length > 5 ? args[5] : "";

        if (groupManager.updateGroup(groupId, name, frequency, prefix, suffix)) {
            sender.sendMessage(ChatColor.GREEN + "Group '" + groupId + "' updated successfully.");
        } else {
            sender.sendMessage(ChatColor.RED + "Group '" + groupId + "' not found.");
        }
    }

    private void handleDeleteGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("advancedrotating.group.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to delete groups.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm delete <groupId>");
            return;
        }

        String groupId = args[1];

        if (groupManager.deleteGroup(groupId)) {
            sender.sendMessage(ChatColor.GREEN + "Group '" + groupId + "' deleted successfully.");
        } else {
            sender.sendMessage(ChatColor.RED + "Group '" + groupId + "' not found.");
        }
    }

    private void handleMessagesCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm messages <group> <list|add|edit|delete|force> [args...]");
            return;
        }

        String groupId = args[1];
        String messageCommand = args[2].toLowerCase();

        MessageGroup group = groupManager.getGroup(groupId);
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "Group '" + groupId + "' not found.");
            return;
        }

        switch (messageCommand) {
            case "list":
                handleListMessages(sender, groupId, args);
                break;
            case "add":
                handleAddMessage(sender, groupId, args);
                break;
            case "edit":
                handleEditMessage(sender, groupId, args);
                break;
            case "delete":
                handleDeleteMessage(sender, groupId, args);
                break;
            case "force":
                handleForceMessage(sender, groupId, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown message command. Use list, add, edit, delete, or force.");
                break;
        }
    }

    private void handleListMessages(CommandSender sender, String groupId, String[] args) {
        if (!sender.hasPermission("advancedrotating.message.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to list messages.");
            return;
        }

        int page = 1;
        if (args.length > 3) {
            try {
                page = Integer.parseInt(args[3]);
                if (page <= 0) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid page number.");
                return;
            }
        }

        int pageSize = 10;
        List<String> messages = groupManager.getMessagesPage(groupId, page, pageSize);
        int totalPages = groupManager.getTotalPages(groupId, pageSize);

        if (messages.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No messages found on page " + page + ".");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Messages in " + groupId + " (Page " + page + "/" + totalPages + ") ===");
        int startIndex = (page - 1) * pageSize;
        for (int i = 0; i < messages.size(); i++) {
            int messageIndex = startIndex + i;
            sender.sendMessage(ChatColor.YELLOW + "[" + messageIndex + "] " + ChatColor.WHITE + messages.get(i));
        }

        if (page < totalPages) {
            sender.sendMessage(ChatColor.GRAY + "Use /arm messages " + groupId + " list " + (page + 1) + " for next page.");
        }
    }

    private void handleAddMessage(CommandSender sender, String groupId, String[] args) {
        if (!sender.hasPermission("advancedrotating.message.add")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to add messages.");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm messages <group> add <message>");
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            if (i > 3) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }

        String message = messageBuilder.toString();

        if (groupManager.addMessage(groupId, message)) {
            sender.sendMessage(ChatColor.GREEN + "Message added to group '" + groupId + "'.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to add message to group '" + groupId + "'.");
        }
    }

    private void handleEditMessage(CommandSender sender, String groupId, String[] args) {
        if (!sender.hasPermission("advancedrotating.message.edit")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to edit messages.");
            return;
        }

        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm messages <group> edit <index> <message>");
            return;
        }

        int messageIndex;
        try {
            messageIndex = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid message index.");
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 4; i < args.length; i++) {
            if (i > 4) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }

        String newMessage = messageBuilder.toString();

        if (groupManager.updateMessage(groupId, messageIndex, newMessage)) {
            sender.sendMessage(ChatColor.GREEN + "Message updated in group '" + groupId + "'.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to update message. Check the index and try again.");
        }
    }

    private void handleDeleteMessage(CommandSender sender, String groupId, String[] args) {
        if (!sender.hasPermission("advancedrotating.message.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to delete messages.");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm messages <group> delete <index>");
            return;
        }

        int messageIndex;
        try {
            messageIndex = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid message index.");
            return;
        }

        if (groupManager.deleteMessage(groupId, messageIndex)) {
            sender.sendMessage(ChatColor.GREEN + "Message deleted from group '" + groupId + "'.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete message. Check the index and try again.");
        }
    }

    private void handleForceMessage(CommandSender sender, String groupId, String[] args) {
        if (!sender.hasPermission("advancedrotating.message.force")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to force send messages.");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /arm messages <group> force <index>");
            return;
        }

        int messageIndex;
        try {
            messageIndex = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid message index.");
            return;
        }

        MessageGroup group = groupManager.getGroup(groupId);
        if (messageIndex < 0 || messageIndex >= group.getMessages().size()) {
            sender.sendMessage(ChatColor.RED + "Invalid message index.");
            return;
        }

        plugin.getMessageScheduler().forceSendMessage(groupId, messageIndex);
        sender.sendMessage(ChatColor.GREEN + "Message sent from group '" + groupId + "'.");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("advancedrotating.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload the plugin.");
            return;
        }

        configManager.reloadConfig();
        plugin.getMessageScheduler().reloadScheduler();
        sender.sendMessage(ChatColor.GREEN + "Advanced Rotating Messages configuration reloaded.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "list", "info", "create", "edit", "delete", "messages", "reload");
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("edit") ||
                   args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("messages"))) {
            for (String groupId : groupManager.getAllGroups().keySet()) {
                if (groupId.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(groupId);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("messages")) {
            List<String> messageCommands = Arrays.asList("list", "add", "edit", "delete", "force");
            for (String messageCommand : messageCommands) {
                if (messageCommand.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(messageCommand);
                }
            }
        }

        return completions;
    }
}