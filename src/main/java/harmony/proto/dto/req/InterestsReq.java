package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class InterestsReq extends BaseDTO {
//    public static final String FETCH = "fetch";
//    public static final String ADD = "add";
//    public static final String REMOVE = "remove";
//    public static final String FETCH_TOP = "fetch_top";

    private String operation;
    private String username;
    private String interest;

    public InterestsReq() {}

    public InterestsReq(String operation, String username, String interest) {
        this.operation = operation;
        this.username = username;
        this.interest = interest;
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getInterest() { return interest; }
    public void setInterest(String interest) { this.interest = interest; }
}