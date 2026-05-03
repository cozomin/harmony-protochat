package harmony.proto.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LoginRes extends BaseRes {
    //TODO: remove username from here because it used to be userID but deletting it would be really complicated agghhhh
    private String username;

    public LoginRes(){
        message = null;
        username = null;
    }//needed for Jackson

    public LoginRes(String message, String username) {
        this.message = message;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    @Override
    public boolean isSuccess() {
        return username != null;
    }
}
