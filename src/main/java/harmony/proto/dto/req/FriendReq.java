package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class FriendReq extends BaseDTO {
    private FriendOperation operation;
    private String user1;
    private String user2;

    FriendReq(){}

    public FriendReq(FriendOperation operation, String user1, String user2) {
        this.operation = operation;
        this.user1 = user1;
        this.user2 = user2;
    }

    public FriendOperation getOperation() {
        return operation;
    }

    public void setOperation(FriendOperation operation) {
        this.operation = operation;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }
}
