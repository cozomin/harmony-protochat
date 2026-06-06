package harmony.proto.client.backend;

import harmony.proto.dto.res.GroupCreationRes;

public interface LiveGroupCreationListener {
    void onNewGroupCreation(GroupCreationRes res);
}
