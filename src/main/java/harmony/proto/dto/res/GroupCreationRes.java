package harmony.proto.dto.res;

import harmony.proto.dto.BaseDTO;
import harmony.proto.dto.UserDTO;

import java.util.List;

public class GroupCreationRes extends BaseRes {
    private String creator;

    public GroupCreationRes() {}

    public GroupCreationRes(String creator, String message) {
        this.creator = creator;
        this.message = message;
    }

    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
}
