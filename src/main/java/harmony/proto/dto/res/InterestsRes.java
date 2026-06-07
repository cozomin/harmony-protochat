package harmony.proto.dto.res;

import java.util.List;

public class InterestsRes extends BaseRes {
    private List<String> interests;

    public InterestsRes() {}

    public InterestsRes(String message, List<String> interests) {
        super(message);
        this.interests = interests;
    }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
}