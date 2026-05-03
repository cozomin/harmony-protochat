package harmony.proto.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import harmony.proto.dto.BaseDTO;
import harmony.proto.dto.MessageDTO;

import java.util.List;

public class MessageRes extends BaseRes {
    private Long number; //if needed in the future for fragmentation
    private List<MessageDTO> chats;

    public MessageRes(){
        message = null;
        number = null;
        chats = null;
    }

    public MessageRes(String message, Long number, List<MessageDTO> chats) {
        this.message = message;
        this.number = number;
        this.chats = chats;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public List<MessageDTO> getChatMessages() {
        return chats;
    }

    public void setChatMessages(List<MessageDTO> chats) {
        this.chats = chats;
    }
}
