package harmony.proto.dto.res;

import harmony.proto.dto.req.ChatMembersReq;

import java.util.List;

public class ChatMembersRes extends BaseRes {
    private List<String> chatMembers;
    private List<String> chatTopics;

    public ChatMembersRes() {}

    public ChatMembersRes(List<String> chatMembers, List<String> chatTopics, String message) {
        super(message);
        this.chatMembers = chatMembers;
        this.chatTopics = chatTopics;
    }

    public List<String> getChatMembers() { return chatMembers; }
    public void setChatMembers(List<String> chatMembers) { this.chatMembers = chatMembers; }

    public List<String> getChatTopics() { return chatTopics; }
    public void setChatTopics(List<String> chatTopics) { this.chatTopics = chatTopics; }
}