package harmony.proto.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import harmony.proto.dto.req.*;
import harmony.proto.dto.res.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)

// ANY NEW SUBCLASS MUST BE ADDED HERE
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = MessageDTO.class, name = "MessDTO"),
        @JsonSubTypes.Type(value = ChatDTO.class, name = "ChatDTO"),
        @JsonSubTypes.Type(value = UserDTO.class, name = "UserDTO"),
        @JsonSubTypes.Type(value = LoginReq.class, name = "LoginReq"),
        @JsonSubTypes.Type(value = LoginRes.class, name = "LoginRes"),
        @JsonSubTypes.Type(value = ChatReq.class, name = "ChatReq"),
        @JsonSubTypes.Type(value = ChatRes.class, name = "ChatRes"),
        @JsonSubTypes.Type(value = MessageReq.class, name = "MessReq"),
        @JsonSubTypes.Type(value = MessageRes.class, name = "MessRes"),
        @JsonSubTypes.Type(value = BaseRes.class, name = "BaseRes"),
        @JsonSubTypes.Type(value = SignUpReq.class, name = "SignUpReq"),
        @JsonSubTypes.Type(value = FriendReq.class, name = "FriendReq"),
        @JsonSubTypes.Type(value = FriendRes.class, name = "FriendRes")
})

public abstract class BaseDTO {
    //empty abstract class for use as an interface with Jackson
}
