package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class FriendReq extends BaseDTO {
    private FriendOperation operation;
    private Long userID1;
    private Long userID2;

    FriendReq(){}

    public FriendReq(FriendOperation operation, Long userID1, Long userID2) {
        this.operation = operation;
        this.userID1 = userID1;
        this.userID2 = userID2;
    }

    public FriendOperation getOperation() {
        return operation;
    }

    public void setOperation(FriendOperation operation) {
        this.operation = operation;
    }

    public Long getUserID1() {
        return userID1;
    }

    public void setUserID1(Long userID1) {
        this.userID1 = userID1;
    }

    public Long getUserID2() {
        return userID2;
    }

    public void setUserID2(Long userID2) {
        this.userID2 = userID2;
    }
}
