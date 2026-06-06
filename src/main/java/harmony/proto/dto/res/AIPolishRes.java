package harmony.proto.dto.res;

public class AIPolishRes extends BaseRes {
    private String polishedText;

    public AIPolishRes() {}

    public AIPolishRes(String message, String polishedText) {
        super(message);
        this.polishedText = polishedText;
    }

    public String getPolishedText() { return polishedText; }
    public void setPolishedText(String polishedText) { this.polishedText = polishedText; }
}