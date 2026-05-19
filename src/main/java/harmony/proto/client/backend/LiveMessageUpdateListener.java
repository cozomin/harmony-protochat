package harmony.proto.client.backend;

import harmony.proto.dto.res.MessageUpdateRes;

public interface LiveMessageUpdateListener {
    void onMessageUpdate(MessageUpdateRes updateEvent);
}