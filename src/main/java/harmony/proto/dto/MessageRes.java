package harmony.proto.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class MessageRes extends BaseDTO{
    private String message;
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

    public List<MessageDTO> getChatMessages() {
        return chats;
    }

    public void setChatMessages(List<MessageDTO> chats) {
        this.chats = chats;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return message.equals("success");
    }
}
