package harmony.proto.dto;

public class UserDTO extends BaseDTO{
    private String username;

    public UserDTO(){
        username = null;
    }

    public UserDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
