# Advanced Rotating Messages Plugin

A comprehensive Minecraft Spigot/PaperMC plugin for managing rotating announcements and tips with advanced scheduling and message tracking capabilities.

## Features

- **Multiple Message Groups**: Create different groups of messages with independent frequencies
- **Smart Message Scheduling**: Ensures all messages in a group are sent before repeating
- **Queue System**: Prevents multiple messages from being sent in the same minute
- **SQLite Database**: Persistent tracking of message states across server restarts
- **Permission System**: Granular permission control for different operations
- **In-Game Management**: Complete command system for managing groups and messages
- **Pagination Support**: Easy browsing of large message lists
- **Color Code Support**: Full Minecraft color and formatting code support

## Requirements

- Java 21 or higher
- Spigot/PaperMC 1.21 or compatible
- SQLite JDBC driver (usually included with most server distributions)

## Installation

1. Download the latest `advanced-rotating-messages.jar` from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. The plugin will create a `advanced-rotating-messages` folder with default configuration

### SQLite Dependency

Most modern Minecraft servers (PaperMC, Spigot) include SQLite support by default. If you encounter a `ClassNotFoundException` for SQLite, download the SQLite JDBC driver:

1. Download `sqlite-jdbc-3.44.1.0.jar` from [Maven Central](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/)
2. Place it in your server's `libs` folder (create the folder if it doesn't exist)
3. Restart your server

## Configuration

The plugin creates a `groups.yml` file in the plugin folder with default message groups:

```yaml
groups:
  announcements:
    name: "Announcements"
    frequency: 5  # minutes
    prefix: "&a[&lANNOUNCEMENT&r&a]&r "
    suffix: ""
    messages:
      0: "Welcome to our server!"
      1: "Don't forget to read the rules!"
      2: "Join our Discord server for updates!"
  tips:
    name: "Tips"
    frequency: 10  # minutes
    prefix: "&e[&lTIP&r&e]&r "
    suffix: ""
    messages:
      0: "Use /spawn to return to spawn!"
      1: "You can use /home to teleport home!"
      2: "Press F3 to see your coordinates!"
```

## Commands

All commands use the base command `/arm` (aliases: `/advancedrotating`, `/rotatingmessages`)

### Group Management
- `/arm list` - List all message groups with their details
- `/arm info <groupId>` - Show detailed information about a specific group
- `/arm create <groupId> <name> <frequency> [prefix] [suffix]` - Create a new message group
- `/arm edit <groupId> <name> <frequency> [prefix] [suffix]` - Edit an existing group
- `/arm delete <groupId>` - Delete a message group

### Message Management
- `/arm messages <groupId> list [page]` - List messages in a group (with pagination)
- `/arm messages <groupId> add <message>` - Add a new message to a group
- `/arm messages <groupId> edit <index> <message>` - Edit an existing message
- `/arm messages <groupId> delete <index>` - Delete a message from a group
- `/arm messages <groupId> force <index>` - Force send a specific message immediately

### Utility
- `/arm help` - Show command help
- `/arm reload` - Reload configuration and restart scheduler

## Permissions

### Admin Permissions
- `advancedrotating.admin` - Full access to all commands (default: op)

### Group Permissions
- `advancedrotating.group.list` - List message groups (default: op)
- `advancedrotating.group.info` - View group information (default: op)
- `advancedrotating.group.create` - Create new groups (default: op)
- `advancedrotating.group.edit` - Edit existing groups (default: op)
- `advancedrotating.group.delete` - Delete groups (default: op)

### Message Permissions
- `advancedrotating.message.list` - List messages in groups (default: op)
- `advancedrotating.message.add` - Add new messages (default: op)
- `advancedrotating.message.edit` - Edit existing messages (default: op)
- `advancedrotating.message.delete` - Delete messages (default: op)
- `advancedrotating.message.force` - Force send messages (default: op)

## How It Works

### Message Scheduling
- Each group has an independent frequency (in minutes)
- Messages are selected randomly from available messages in the pool
- Once a message is sent, it's marked as used and won't be sent again until all other messages in the group have been sent
- When all messages in a group have been sent, the pool resets and the cycle starts over

### Queue System
- Only one message can be sent per minute server-wide
- If multiple groups are scheduled to send messages at the same time, they are queued
- Queued messages are sent in subsequent minutes to maintain the one-message-per-minute limit

### Database Tracking
- SQLite database tracks which messages have been sent for each group
- Message states persist across server restarts and crashes
- New messages added to groups are automatically added to the available pool

## Building from Source

### Prerequisites
- Java 21 JDK
- Maven 3.6+

### Build Commands
```bash
# Clone the repository
git clone <repository-url>
cd advanced-rotating-messages

# Build the plugin
mvn clean compile package

# The JAR file will be created in target/advanced-rotating-messages.jar
```

### GitHub Actions
The project includes automated building and releases:
- Builds are triggered on every push to master
- Automatic releases are created with pre-compiled JARs
- Build artifacts are available for download from Actions

## Support

For support, bug reports, or feature requests, please create an issue on the GitHub repository.

## License

This project is licensed under the MIT License - see the LICENSE file for details.