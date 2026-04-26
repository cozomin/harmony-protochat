package harmony.proto.database;

import java.time.Instant;

public class Message {
    private Long messId;
    private Long senderId;
    private Long chatId;
    private String content;
    private Instant sentAt;
    private String messageType;

    public Message() {}

    public Message(Long messId, Long senderId, Long chatId, String content, Instant sentAt, String messageType) {
        this.messId = messId;
        this.senderId = senderId;
        this.chatId = chatId;
        this.content = content;
        this.sentAt = sentAt;
        this.messageType = messageType;
    }

    public Long getMessId() { return messId; }
    public void setMessId(Long messId) { this.messId = messId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}