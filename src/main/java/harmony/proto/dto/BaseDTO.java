package harmony.proto.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)

@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = MessageDTO.class, name = "MessDTO"),
        @JsonSubTypes.Type(value = ChatDTO.class, name = "ChatDTO"),
        @JsonSubTypes.Type(value = UserDTO.class, name = "UserDTO"),
        @JsonSubTypes.Type(value = LoginReq.class, name = "LoginReq"),
        @JsonSubTypes.Type(value = LoginRes.class, name = "LoginRes")
})

public abstract class BaseDTO {
    //empty abstract class for use as an interface with Jackson
}
