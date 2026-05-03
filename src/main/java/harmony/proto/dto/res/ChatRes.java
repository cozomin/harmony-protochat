package harmony.proto.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import harmony.proto.dto.BaseDTO;
import harmony.proto.dto.ChatDTO;

import java.util.List;

public class ChatRes extends BaseRes {
    private Long number; //if needed in the future for fragmentation
    private List<ChatDTO> chats;

    public ChatRes(){
        message = null;
        number = null;
        chats = null;
    }

    public ChatRes(String message, Long number, List<ChatDTO> chats) {
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

    public List<ChatDTO> getChats() {
        return chats;
    }

    public void setChats(List<ChatDTO> chats) {
        this.chats = chats;
    }
}
