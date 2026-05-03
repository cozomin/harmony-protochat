package harmony.proto.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import harmony.proto.dto.BaseDTO;

public class BaseRes extends BaseDTO {
    protected String message;

    public BaseRes(){}

    public BaseRes(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return message.equals("success");
    }
}
