package harmony.proto.database;

import java.time.Instant;

public class Chat {
    private Long chatID;
    private String chatName;
    private boolean isGroup;
    private Instant updated_at;

    public Chat() {}

    public Chat(Long chatID, String chatName, boolean isGroup, Instant updated_at) {
        this.chatID = chatID;
        this.chatName = chatName;
        this.isGroup = isGroup;
        this.updated_at = updated_at;
    }

    public Long getChatID() {
        return chatID;
    }

    public String getChatName() {
        return chatName;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }

    public void setChatID(Long chatID) {
        this.chatID = chatID;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public void setUpdated_at(Instant updated_at) {
        this.updated_at = updated_at;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
