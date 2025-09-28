# Advanced Rotating Announcement and Tip Plugin

This is a Minecraft Spigot/PaperMC based server plugin that is designed to be an advanced rotating announcement/tip plugin for Spigot and PaperMC based Minecraft servers. Targeting servers for Minecraft release 1.21.8

## Requirements

There are several requirements for this plugin which will be outlined below:

- Have "groups" of differing rotating messages - different groups of messages can have different frequencies for how often the messages are sent to the chat of the server
  - This will allow less tips to be broadcast at differing frequency than an important announcement for example
  - These groups will also support different prefix and suffixes to allow them to have standard formatting/colouring for the group - However colour and formatting codes can also be used within the configured messages to also allow additional per message formatting
- All of the settings for this project, including the messages will be stored in a user friendly editable configuration format such as YAML.
- Users should be able to utilize commands in game to manage what is stored in these configurations. Such as:
  - listing the groups that are configured (when listed, it should also indicate how many messages are configured in each group, and their frequency)
  - get info on a specific group so that you can see details on messages stored in the group, frequency, prefix, suffix, etc
  - adding new groups (defining the name, frequency of messages, prefix, suffix)
  - editing existing groups (defining the name, frequency of messages, prefix, suffix)
  - deleting existing groups
  - listing the messages within groups (pagination system will likely be required for this as there could be many messages in a group)
  - editing a message within the group
  - adding a new message within the group
  - delete a message from within a group
  - Force send a message from within a group (if the message was still in the pool for it's group to be sent, this would remove it from the pool for that cycle, it however will not adjust the frequency of when the next message in the group gets sent, this will just be an additional message)
- The level of precision for the frequencies should be in minutes only
- We want to only send a max of one message in a minute, because each group has differing frequencies there may sometimes be overlap on the timings, when this occurs the overlapping message should be added to a queue and it should be sent in the following minute to ensure a max of only one message per minute is sent.
- Some kind of mini database may be needed with something like sqlite, or json for example. We want to send messages from a group randomly, however we want to ensure every message gets sent once, before a message gets sent again. For example, if a group has 5 messages within it, and message #2 gets randomly chosen to be sent, we do not want to send message #2 again, until #1,3,4,5 all have gotten sent. If a new message gets added to a group mid-cycle, it can get added into the pool of messages that still need to be randomly sent. We need to track this in some kind of way to ensure between restarts/crashes we still know where we were at. I always want the messages being sent randomly out of the group. So once it has gone through a cycle of sending every message in a group, it should once again add all the messages in the group back to the pool and begin randomly sending them again
- The commands that are added should have a refined permission node system allowing server owners to correctly assign staff permissions to use this system - Anyone with the OP status also would be allowed to use the commands

## Building

The system should have some kind of build system/tool to allow easily compiling a JAR file. This project will be utilizing JAVA 21 with JDK 21. This jar file should be something a Minecraft server owner could drop in their plugins folder. When the plugin is first run, it will generate any config files needed within a new folder it creates in the plugins folder. The JAR file will be named "advanced-rotating-messages.jar" and will created a folder called "advanced-rotating-messages" when the plugin is first run on a Minecraft Server.

### Github CLI / Actions

We should include Github Actions configuration files to allow the project to be automatically built when there is a commit to any branch. It should generate a new release with the built pre-compiled JAR file on every commit to master.
