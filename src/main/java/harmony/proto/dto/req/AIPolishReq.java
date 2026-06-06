package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class AIPolishReq extends BaseDTO {
    private String originalText;

    public AIPolishReq() {}

    public AIPolishReq(String originalText) {
        this.originalText = originalText;
    }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
}