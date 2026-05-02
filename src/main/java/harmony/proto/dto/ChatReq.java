package harmony.proto.dto;

public class ChatReq extends BaseDTO{
    private Long userID;

    public ChatReq(Long userID) {
        this.userID = userID;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }
}
