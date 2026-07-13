package org.nivaris.party.event;

import org.allaymc.api.eventbus.event.CancellableEvent;
import org.nivaris.party.Party;

public class PartyDisposeEvent extends PartyEvent implements CancellableEvent {

    public PartyDisposeEvent(Party party) {
        super(party);
    }
}
