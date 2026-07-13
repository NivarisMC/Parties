package org.nivaris.party;

import org.allaymc.api.player.Player;

import java.util.HashMap;
import java.util.Map;

public class InvitationManager {

    private final Map<String, Invitation> invitations = new HashMap<>();

    public Invitation getInvitation(Player player) {
        return invitations.computeIfAbsent(player.getLoginData().getXname(), Invitation::new);
    }

    public void removeInvitation(Player player) {
        invitations.remove(player.getLoginData().getXname());
    }

    public void removeInviter(Player player) {
        String name = player.getLoginData().getXname();
        for (Invitation invitation : invitations.values()) {
            invitation.removeInviter(player);
        }
    }

    public void removeInvitersByParty(Party party) {
        for (Invitation invitation : invitations.values()) {
            invitation.removeInviterByParty(party);
        }
    }
}
