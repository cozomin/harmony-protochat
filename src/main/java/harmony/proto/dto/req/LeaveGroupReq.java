package harmony.proto.dto.req;
import harmony.proto.dto.BaseDTO;

public class LeaveGroupReq extends BaseDTO {
    private String groupName;
    public LeaveGroupReq() {}
    public LeaveGroupReq(String groupName) { this.groupName = groupName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
}