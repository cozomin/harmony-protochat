package harmony.proto.dto.res;
import java.util.List;

public class AIRecommendGroupsRes extends BaseRes {
    private List<String> recommendedGroups;
    public AIRecommendGroupsRes() {}
    public AIRecommendGroupsRes(String message, List<String> recommendedGroups) {
        super(message);
        this.recommendedGroups = recommendedGroups;
    }
    public List<String> getRecommendedGroups() { return recommendedGroups; }
    public void setRecommendedGroups(List<String> recommendedGroups) { this.recommendedGroups = recommendedGroups; }
}