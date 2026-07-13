package org.nivaris.party;

import org.allaymc.api.player.Player;
import org.nivaris.party.event.PartyCreateEvent;
import org.nivaris.party.Messages;
import org.nivaris.party.event.PartyDisposeEvent;
import org.nivaris.party.event.PartyLeaveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyManager {

    private final Map<String, Party> parties = new HashMap<>();

    public Party getPlayerParty(Player player) {
        for (Party party : parties.values()) {
            if (party.isMember(player)) return party;
        }
        return null;
    }

    public void createParty(Player player, boolean isPublic) throws PartyException {
        Party party = getPlayerParty(player);
        if (party != null) throw new PartyException(Messages.get("error.already_has_party"));

        party = new Party(this, player.getLoginData().getXname(), isPublic);

        PartyCreateEvent ev = new PartyCreateEvent(party);
        ev.call();

        if (ev.isCancelled()) throw new PartyException(Messages.get("error.create_cancelled"));

        parties.put(player.getLoginData().getXname(), party);
    }

    public void disposeParty(Player player) throws PartyException {
        Party party = getPlayerParty(player);
        if (party == null) throw new PartyException(Messages.get("error.no_party"));
        if (!party.isOwner(player)) throw new PartyException(Messages.get("error.not_owner"));

        PartyDisposeEvent ev = new PartyDisposeEvent(party);
        ev.call();

        if (ev.isCancelled()) throw new PartyException(Messages.get("error.dispose_failed"));

        party.broadcastMessage(Messages.get("party.dispose.broadcast"), List.of(party.getOwner()));

        Main.getInstance().getInvitationManager().removeInvitersByParty(party);
        parties.remove(player.getLoginData().getXname());
    }

    public void leaveParty(Player player) throws PartyException {
        leaveParty(player, false);
    }

    public void leaveParty(Player player, boolean kick) throws PartyException {
        Party party = getPlayerParty(player);
        if (party == null) throw new PartyException(Messages.get("error.no_party"));
        if (party.isOwner(player)) {
            disposeParty(player);
            return;
        }

        PartyLeaveEvent ev = new PartyLeaveEvent(player, party);
        ev.call();
        if (ev.isCancelled()) return;

        party.removeMember(player);
        Main.getInstance().getInvitationManager().removeInvitation(player);

        String name = player.getLoginData().getXname();
        if (kick) {
            player.sendMessage(Messages.get("party.kick.target"));
            party.broadcastMessage(Messages.format("party.kick.broadcast", name));
            return;
        }

        party.broadcastMessage(Messages.format("party.leave.broadcast", name));
    }

    public void inviteParty(Player invitor, String targetName) throws PartyException {
        Party party = getPlayerParty(invitor);
        if (party == null) throw new PartyException(Messages.get("error.no_party"));
        party.invite(invitor, targetName);
    }

    public void autoCreatedParty(Map<String, Object> data) {
        try {
            Party party = Party.decode(this, data);
            if (!parties.containsKey(party.getOwner())) {
                parties.put(party.getOwner(), party);
            }
        } catch (PartyException e) {
            Main.getInstance().getPluginLogger().error(e.getMessage());
        }
    }

    public Map<String, Party> getParties() {
        return parties;
    }
}
