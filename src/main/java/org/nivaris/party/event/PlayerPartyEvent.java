package org.nivaris.party.event;

import lombok.Getter;
import org.allaymc.api.player.Player;
import org.nivaris.party.Party;

@Getter
public class PlayerPartyEvent extends PartyEvent {

    protected Player player;

    public PlayerPartyEvent(Player player, Party party) {
        super(party);
        this.player = player;
    }
}
