package harmony.proto.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import harmony.proto.dto.BaseDTO;

public class LoginRes extends BaseRes {
    private Long userID;

    public LoginRes(){
        message = null;
        userID = null;
    }//needed for Jackson

    public LoginRes(String message, Long userID) {
        this.message = message;
        this.userID = userID;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    @JsonIgnore
    @Override
    public boolean isSuccess() {
        return userID != null && userID != -1;
    }
}
