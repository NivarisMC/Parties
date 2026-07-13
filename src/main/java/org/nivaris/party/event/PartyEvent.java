package org.nivaris.party.event;

import lombok.Getter;
import org.allaymc.api.eventbus.event.Event;
import org.nivaris.party.Party;

@Getter
public class PartyEvent extends Event {

    protected Party party;

    public PartyEvent(Party party) {
        this.party = party;
    }
}
