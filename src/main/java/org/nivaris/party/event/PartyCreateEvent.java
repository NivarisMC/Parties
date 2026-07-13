package org.nivaris.party.event;

import lombok.Getter;
import org.allaymc.api.eventbus.event.CancellableEvent;
import org.nivaris.party.Party;

@Getter
public class PartyCreateEvent extends PartyEvent implements CancellableEvent {

    public PartyCreateEvent(Party party) {
        super(party);
    }

    public boolean isPublic() {
        return party.isPublic();
    }
}
