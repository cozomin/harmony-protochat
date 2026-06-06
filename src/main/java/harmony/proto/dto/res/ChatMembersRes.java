package harmony.proto.dto.res;

import harmony.proto.dto.req.ChatMembersReq;

import java.util.List;

public class ChatMembersRes extends BaseRes {
    private List<String> chatMembers;

    public ChatMembersRes() {}

    public ChatMembersRes(List<String> chatMembers, String message) {
        this.chatMembers = chatMembers;
        this.message = message;
    }

    public List<String> getChatMembers() {
        return chatMembers;
    }

    public void setChatMembers(List<String> chatMembers) {
        this.chatMembers = chatMembers;
    }
}
