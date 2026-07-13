package org.nivaris.party;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PartyTransferManager {

    private final Map<String, PartyTransferQueue> queues = new HashMap<>();

    public Collection<PartyTransferQueue> getQueues() {
        return queues.values();
    }

    public void addQueue(Party party, String address) {
        String key = party.getOwner();
        if (queues.containsKey(key)) return;
        queues.put(key, new PartyTransferQueue(party, address));
    }

    public PartyTransferQueue getQueue(Party party) {
        return queues.get(party.getOwner());
    }

    public void removeQueue(Party party) {
        queues.remove(party.getOwner());
    }
}
