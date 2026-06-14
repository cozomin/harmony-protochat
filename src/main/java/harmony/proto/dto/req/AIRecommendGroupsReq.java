package harmony.proto.dto.req;
import harmony.proto.dto.BaseDTO;

public class AIRecommendGroupsReq extends BaseDTO {
    private String topic;
    public AIRecommendGroupsReq() {}
    public AIRecommendGroupsReq(String topic) { this.topic = topic; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}