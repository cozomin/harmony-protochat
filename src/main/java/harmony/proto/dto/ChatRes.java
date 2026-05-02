package harmony.proto.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

public class ChatRes extends BaseDTO{
    private String message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    @JsonIgnore
    public boolean isSuccess(){
        return message.equals("success");
    }
}
