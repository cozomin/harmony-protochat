package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class ChatReq extends BaseDTO {
    private String username;

    public ChatReq() {}

    public ChatReq(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
