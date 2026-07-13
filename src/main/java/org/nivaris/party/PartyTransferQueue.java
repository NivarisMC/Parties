package org.nivaris.party;

import org.allaymc.api.player.Player;

public class PartyTransferQueue {

    private static final int DELAY_TICKS = 4 * 20;

    private final Party party;
    private final String address;
    private int delay;

    public PartyTransferQueue(Party party, String address) {
        this.party = party;
        this.address = address;
        this.delay = DELAY_TICKS;
    }

    public boolean run() {
        if (delay > 0) {
            delay--;
            return false;
        }

        for (Player player : party.getMembersAsClass()) {
            player.transfer(address, 0);
        }

        return true;
    }

    public Party getParty() {
        return party;
    }

    public String getAddress() {
        return address;
    }
}
