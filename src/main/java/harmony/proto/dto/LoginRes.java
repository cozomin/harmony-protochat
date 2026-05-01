package harmony.proto.dto;

public class LoginRes extends BaseDTO{
    private String message;
    private Long UserID;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserID() {
        return UserID;
    }

    public void setUserID(Long userID) {
        UserID = userID;
    }
}
