package org.nivaris.party;

import org.allaymc.api.player.Player;
import org.allaymc.api.server.Server;
import org.nivaris.party.Messages;

import java.util.HashMap;
import java.util.Map;

public class Invitation {

    private final String player;
    private final Map<String, Party> inviters = new HashMap<>();

    public Invitation(String player) {
        this.player = player;
    }

    public Player getPlayerClass() {
        return Server.getInstance().getPlayerManager().getPlayerByName(player);
    }

    public int getCountInvitor() {
        return inviters.size();
    }

    public Map<String, Party> getInviters() {
        return inviters;
    }

    public void addInvitor(Player invitor, Party party) throws PartyException {
        String name = invitor.getLoginData().getXname();
        if (inviters.containsKey(name)) {
            throw new PartyException(Messages.get("error.already_invited"));
        }
        inviters.put(name, party);
    }

    public void removeInviter(Player invitor) {
        inviters.remove(invitor.getLoginData().getXname());
    }

    public void removeInviterByParty(Party party) {
        inviters.values().removeIf(p -> p.getOwner().equals(party.getOwner()));
    }

    public void accept(String invitorName) throws PartyException {
        Party party = inviters.get(invitorName);
        if (party == null) throw new PartyException(Messages.get("error.party_not_found"));
        if (party.isFull()) throw new PartyException(Messages.get("error.party_full"));
        Player player = getPlayerClass();
        if (player == null) throw new PartyException(Messages.get("error.party_load_failed"));

        party.addMember(player);
        inviters.remove(invitorName);
    }

    public void reject(String invitorName) throws PartyException {
        Party party = inviters.get(invitorName);
        if (party == null) throw new PartyException(Messages.get("error.party_not_found"));
        inviters.remove(invitorName);
    }
}
