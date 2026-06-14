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
        @JsonSubTypes.Type(value = FriendRes.class, name = "FriendRes"),
        @JsonSubTypes.Type(value = GroupCreationReq.class, name = "GroupCreationReq"),
        @JsonSubTypes.Type(value = GroupCreationRes.class, name = "GroupCreationRes"),
        @JsonSubTypes.Type(value = ChatMembersReq.class, name = "ChatMembersReq"),
        @JsonSubTypes.Type(value = ChatMembersRes.class, name = "ChatMembersRes"),
        @JsonSubTypes.Type(value = MessageEditReq.class, name = "MessageEditReq"),
        @JsonSubTypes.Type(value = MessageDeleteReq.class, name = "MessageDeleteReq"),
        @JsonSubTypes.Type(value = MessageUpdateRes.class, name = "MessageUpdateRes"),
        @JsonSubTypes.Type(value = AIPolishReq.class, name = "AIPolishReq"),
        @JsonSubTypes.Type(value = AIPolishRes.class, name = "AIPolishRes"),
        @JsonSubTypes.Type(value = InterestsReq.class, name = "InterestsReq"),
        @JsonSubTypes.Type(value = InterestsRes.class, name = "InterestsRes"),
        @JsonSubTypes.Type(value = AIRecommendGroupsReq.class, name = "AIRecommendGroupsReq"),
        @JsonSubTypes.Type(value = AIRecommendGroupsRes.class, name = "AIRecommendGroupsRes"),
        @JsonSubTypes.Type(value = JoinGroupReq.class, name = "JoinGroupReq"),
        @JsonSubTypes.Type(value = JoinGroupRes.class, name = "JoinGroupRes")
})

public abstract class BaseDTO {
    //empty abstract class for use as an interface with Jackson
}
