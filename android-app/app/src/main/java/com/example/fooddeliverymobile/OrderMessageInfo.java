package com.example.fooddeliverymobile;

public class OrderMessageInfo {
    private long id;
    private long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private String sentAt;

    public String getContent() { return content == null ? "" : content; }

    @Override
    public String toString() {
        String author = senderName == null || senderName.isBlank() ? "Naudotojas" : senderName;
        String time = sentAt == null ? "" : sentAt.replace('T', ' ');
        return author + " · " + time + "\n" + getContent();
    }
}
