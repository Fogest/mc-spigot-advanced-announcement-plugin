# Testing Guide for Advanced Rotating Messages Plugin

## Prerequisites
- Minecraft server with PaperMC 1.21.8
- Java 21
- OP permissions or plugin permissions

## Installation Testing

1. **Plugin Loading Test**
   - Start your Minecraft server
   - Check console for: `AdvancedRotatingMessages is starting up...`
   - Check for: `AdvancedRotatingMessages has been enabled successfully!`
   - Verify plugin folder created: `plugins/advanced-rotating-messages/`
   - Verify config file created: `plugins/advanced-rotating-messages/groups.yml`
   - Verify database created: `plugins/advanced-rotating-messages/messages.db`

## Basic Command Testing

### 1. Help Command
```
/arm help
```
Expected: List of all available commands with descriptions

### 2. List Groups
```
/arm list
```
Expected: Shows default groups (announcements, tips) with message counts and frequencies

### 3. Group Information
```
/arm info announcements
/arm info tips
```
Expected: Detailed information about each group including name, frequency, prefix, suffix, and message count

## Group Management Testing

### 4. Create New Group
```
/arm create alerts "Server Alerts" 3 "&c[&lALERT&r&c]&r " ""
```
Expected: Success message confirming group creation

### 5. Edit Group
```
/arm edit alerts "Important Alerts" 2 "&4[&lIMPORTANT&r&4]&r " ""
```
Expected: Success message confirming group update

### 6. Verify Changes
```
/arm info alerts
/arm list
```
Expected: Shows updated group information

## Message Management Testing

### 7. Add Messages to New Group
```
/arm messages alerts add "Server restart in 30 minutes!"
/arm messages alerts add "Remember to back up your builds!"
/arm messages alerts add "New event starting soon!"
```
Expected: Success messages for each addition

### 8. List Messages
```
/arm messages alerts list
/arm messages announcements list
```
Expected: Shows all messages with indices, paginated if many messages

### 9. Edit Message
```
/arm messages alerts edit 0 "Server restart in 15 minutes!"
```
Expected: Success message confirming edit

### 10. Force Send Message
```
/arm messages alerts force 0
```
Expected: Message broadcasts immediately to all players

### 11. Delete Message
```
/arm messages alerts delete 2
```
Expected: Success message, message removed from group

## Advanced Testing

### 12. Test Pagination
```
/arm messages announcements add "Message 1"
/arm messages announcements add "Message 2"
/arm messages announcements add "Message 3"
/arm messages announcements add "Message 4"
/arm messages announcements add "Message 5"
/arm messages announcements add "Message 6"
/arm messages announcements add "Message 7"
/arm messages announcements add "Message 8"
/arm messages announcements add "Message 9"
/arm messages announcements add "Message 10"
/arm messages announcements add "Message 11"
/arm messages announcements list 1
/arm messages announcements list 2
```
Expected: First page shows 10 messages, second page shows remaining

### 13. Test Automatic Message Rotation
- Wait for 5 minutes (announcements frequency)
- Wait for 10 minutes (tips frequency)
- Wait for 2 minutes (alerts frequency)
Expected: Messages appear automatically at specified intervals

### 14. Test Color Codes
```
/arm messages announcements add "&6Welcome &l&nNEW PLAYERS&r&6 to our server!"
/arm messages announcements force 3
```
Expected: Message displays with colors and formatting

### 15. Test Reload
```
/arm reload
```
Expected: Configuration reloaded, success message shown

### 16. Delete Group
```
/arm delete alerts
```
Expected: Group and all its messages removed

## Error Testing

### 17. Test Invalid Commands
```
/arm nonexistent
/arm info nonexistent
/arm messages nonexistent list
/arm messages announcements force 999
/arm edit nonexistent "test" 5
```
Expected: Appropriate error messages for each

### 18. Test Permission Restrictions
- Remove OP status or specific permissions
- Try commands
Expected: Permission denied messages

## Server Restart Testing

### 19. Test Persistence
1. Add some messages to groups
2. Wait for some messages to be sent
3. Restart server
4. Check that message pools are maintained correctly
5. Verify automatic messaging resumes properly

## Performance Testing

### 20. Test Player-Aware Broadcasting
1. Set up a group with a short frequency (1-2 minutes for quick testing)
2. Start the server with no players
3. Wait for several frequency cycles
4. Check logs - should see "Skipping message processing - no players online" (in debug/fine logs)
5. Join the server as a player
6. Verify messages start broadcasting again

### 21. Stress Test
- Create multiple groups with many messages
- Verify server performance remains stable
- Check that only one message per minute is sent even with multiple groups

## Expected Automatic Behavior

Once the plugin is loaded with default configuration:
- Every 5 minutes: An announcement message should broadcast (when players are online)
- Every 10 minutes: A tip message should broadcast (when players are online)
- Messages are selected randomly but ensure all messages are sent before repeating
- Maximum of one message per minute across all groups
- If multiple groups are due, messages are queued for subsequent minutes
- **Smart Pausing**: No messages are sent when the server is empty (0 players online)

## Troubleshooting

**Plugin doesn't load:**
- Check Java version (requires Java 21+)
- Check server logs for errors
- Verify JAR file integrity

**Messages not sending:**
- Check group frequencies are reasonable
- Verify groups have messages
- Check server logs for scheduling errors

**Database issues:**
- Check file permissions in plugin folder
- Verify SQLite database file exists and is accessible
- Check for disk space issues

**Permission issues:**
- Verify OP status or specific plugin permissions
- Check permission plugin compatibility if using one