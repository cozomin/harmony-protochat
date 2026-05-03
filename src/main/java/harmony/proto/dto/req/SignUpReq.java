package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class SignUpReq extends LoginReq {

    public SignUpReq() {
    }

    public SignUpReq(String username, String password) {
        super(username, password);
    }
}
