package org.nivaris.party;

import org.allaymc.api.player.Player;
import org.allaymc.api.server.Server;
import org.nivaris.party.Messages;

import java.util.*;

public class Party {

    public static final String PARTY_PROXY_KEY = "party_proxy_data";
    public static final String PARTY_MEMBERS_KEY = "party_members_data";
    public static final String PARTY_OWNER_KEY = "party_owner_data";

    private static final int MAX_MEMBERS = 4;

    private final PartyManager partyManager;
    private final String owner;
    private boolean isPublic;
    private final Map<String, String> members;

    public Party(PartyManager partyManager, String owner, boolean isPublic) {
        this(partyManager, owner, isPublic, new HashMap<>());
    }

    public Party(PartyManager partyManager, String owner, boolean isPublic, Map<String, String> members) {
        this.partyManager = partyManager;
        this.owner = owner;
        this.isPublic = isPublic;
        this.members = new HashMap<>(members);
        this.members.put(owner, owner);
    }

    public String getOwner() {
        return owner;
    }

    public boolean isOwner(Player player) {
        return player.getLoginData().getXname().equals(owner);
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic() {
        this.isPublic = true;
    }

    public void unPublic() {
        this.isPublic = false;
    }

    public boolean isMember(Player player) {
        return members.containsKey(player.getLoginData().getXname());
    }

    public Player getPlayerClass(String name) {
        if (!members.containsKey(name)) return null;
        return Server.getInstance().getPlayerManager().getPlayerByName(name);
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public List<Player> getMembersAsClass() {
        List<Player> result = new ArrayList<>();
        for (String member : members.keySet()) {
            Player player = getPlayerClass(member);
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    public int getMembersCount() {
        return members.size();
    }

    public void addMember(Player player) {
        String name = player.getLoginData().getXname();
        members.put(name, name);
        broadcastMessage(Messages.format("party.join.broadcast", name), List.of(name));
    }

    public void removeMember(Player player) {
        members.remove(player.getLoginData().getXname());
    }

    public boolean isFull() {
        return members.size() >= MAX_MEMBERS;
    }

    public void invite(Player invitor, String playerName) throws PartyException {
        Player player = Server.getInstance().getPlayerManager().getPlayerByName(playerName);
        if (player == null) throw new PartyException(Messages.get("error.player_not_found"));

        Party party = partyManager.getPlayerParty(player);
        if (party != null) throw new PartyException(Messages.get("error.player_already_in_party"));

        Invitation invitation = Main.getInstance().getInvitationManager().getInvitation(player);
        invitation.addInvitor(invitor, this);
        player.sendMessage(Messages.format("party.invite.received", invitor.getLoginData().getXname(), owner));
    }

    public void broadcastMessage(String message, Collection<String> except) {
        for (String member : members.keySet()) {
            if (except.contains(member)) continue;
            Player player = Server.getInstance().getPlayerManager().getPlayerByName(member);
            if (player == null) continue;
            player.sendMessage(message);
        }
    }

    public void broadcastMessage(String message) {
        broadcastMessage(message, List.of());
    }

    public Map<String, Object> encode() {
        Map<String, Object> data = new HashMap<>();
        data.put(PARTY_OWNER_KEY, owner);
        data.put(PARTY_MEMBERS_KEY, new HashMap<>(members));
        return data;
    }

    @SuppressWarnings("unchecked")
    public static Party decode(PartyManager manager, Map<String, Object> data) throws PartyException {
        if (!data.containsKey(PARTY_OWNER_KEY) || !data.containsKey(PARTY_MEMBERS_KEY)) {
            throw new PartyException("Data socket error");
        }
        Map<String, String> members = (Map<String, String>) data.get(PARTY_MEMBERS_KEY);
        return new Party(manager, (String) data.get(PARTY_OWNER_KEY), false, members);
    }
}
