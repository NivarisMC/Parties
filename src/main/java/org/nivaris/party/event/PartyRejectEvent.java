package org.nivaris.party.event;

import org.allaymc.api.player.Player;
import org.allaymc.api.eventbus.event.CancellableEvent;
import org.nivaris.party.Party;

public class PartyRejectEvent extends PlayerPartyEvent implements CancellableEvent {

    public PartyRejectEvent(Player player, Party party) {
        super(player, party);
    }
}
