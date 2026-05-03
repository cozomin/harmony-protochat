package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class MessageReq extends BaseDTO {
//    private Long userID;

    private Long chatID;

    public MessageReq() {}

    public MessageReq(Long chatID) {
//        this.userID = userID;
        this.chatID = chatID;
    }

//    public Long getUserID() {return userID;}

//    public void setUserID(Long userID) {
//        this.userID = userID;
//    }

    public Long getChatID() {
        return chatID;
    }

    public void setChatID(Long chatID) {
        this.chatID = chatID;
    }
}
