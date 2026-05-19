package harmony.proto.dto.res;

import harmony.proto.dto.res.BaseRes;

public class MessageUpdateRes extends BaseRes {
    private Long messId;

    private MessageUpdateAction action;

    private String newContent;

    public MessageUpdateRes() {
    }

    public MessageUpdateRes(Long messId, MessageUpdateAction action, String newContent) {
        this.messId = messId;
        this.action = action;
        this.newContent = newContent;
    }

    public Long getMessId() {
        return messId;
    }

    public void setMessId(Long messId) {
        this.messId = messId;
    }

    public MessageUpdateAction getAction() {
        return action;
    }

    public void setAction(MessageUpdateAction action) {
        this.action = action;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}