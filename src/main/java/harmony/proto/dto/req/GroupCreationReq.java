package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;
import harmony.proto.dto.UserDTO;

import java.util.List;

public class GroupCreationReq extends BaseDTO {
    private String name;
    private List<String> topics;
    private String creator;
    private List<String> members;
    private String description;

    public GroupCreationReq() {}
    public GroupCreationReq(String name, List<String> topics, String description, List<String> members, String creator) {
        this.name = name;
        this.topics = topics;
        this.description = description;
        this.members = members;
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
}
