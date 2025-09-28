package com.advancedrotating.messages.models;

import java.util.ArrayList;
import java.util.List;

public class MessageGroup {
    private String id;
    private String name;
    private int frequency;
    private String prefix;
    private String suffix;
    private List<String> messages;

    public MessageGroup(String id, String name, int frequency, String prefix, String suffix, List<String> messages) {
        this.id = id;
        this.name = name;
        this.frequency = frequency;
        this.prefix = prefix;
        this.suffix = suffix;
        this.messages = new ArrayList<>(messages);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = new ArrayList<>(messages);
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public void removeMessage(int index) {
        if (index >= 0 && index < messages.size()) {
            messages.remove(index);
        }
    }

    public void updateMessage(int index, String newMessage) {
        if (index >= 0 && index < messages.size()) {
            messages.set(index, newMessage);
        }
    }

    public int getMessageCount() {
        return messages.size();
    }
}