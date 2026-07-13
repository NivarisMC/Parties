package org.nivaris.party.event;

import lombok.Getter;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.event.CancellableEvent;
import org.nivaris.party.Invitation;
import org.nivaris.party.Party;

@Getter
public class PartyInviteEvent extends PartyEvent implements CancellableEvent {

    private final EntityPlayer invitor;
    private final Invitation invitation;

    public PartyInviteEvent(EntityPlayer invitor, Party party, Invitation invitation) {
        super(party);
        this.invitor = invitor;
        this.invitation = invitation;
    }
}
