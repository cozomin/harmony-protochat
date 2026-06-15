package harmony.proto.dto.res;

import harmony.proto.dto.MessageDTO;

import java.util.List;

public class MessageRes extends BaseRes {
    private Long number; //if needed in the future for fragmentation
    private List<MessageDTO> messages;

    public MessageRes(){
        message = null;
        number = null;
        messages = null;
    }

    public MessageRes(String message, Long number, List<MessageDTO> messages) {
        this.message = message;
        this.number = number;
        this.messages = messages;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public List<MessageDTO> getChatMessages() {
        return messages;
    }

    public void setChatMessages(List<MessageDTO> chats) {
        this.messages = chats;
    }
}
