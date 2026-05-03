package harmony.proto.dto.res;

import com.fasterxml.jackson.databind.ser.Serializers;
import harmony.proto.dto.UserDTO;
import harmony.proto.dto.req.FriendOperation;

import java.util.List;

public class FriendRes extends BaseRes {
    private FriendOperation operation;
    private List<UserDTO> users;

    FriendRes(){
        message = null;
        operation = null;
        users = null;
    }

    public FriendRes(FriendOperation operation, List<UserDTO> users) {
        this.operation = operation;
        this.users = users;
    }

    public FriendRes(String message, FriendOperation operation, List<UserDTO> users) {
        super(message);
        this.operation = operation;
        this.users = users;
    }

    public FriendOperation getOperation() {
        return operation;
    }

    public void setOperation(FriendOperation operation) {
        this.operation = operation;
    }

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }
}
