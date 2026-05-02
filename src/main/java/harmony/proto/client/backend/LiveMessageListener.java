package harmony.proto.client.backend;

import harmony.proto.dto.MessageDTO;

public interface LiveMessageListener {
    void onNewMessage(MessageDTO message);
}
