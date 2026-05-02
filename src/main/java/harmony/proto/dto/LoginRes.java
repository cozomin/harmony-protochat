package harmony.proto.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LoginRes extends BaseDTO{
    private String message;
    private Long userID;

    public LoginRes(){
        message = null;
        userID = null;
    }//needed for Jackson

    public LoginRes(String message, Long userID) {
        this.message = message;
        this.userID = userID;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return userID != null && userID != -1;
    }
}
