package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class ChatReq extends BaseDTO {
    private Long userID;

    public ChatReq() {}

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
