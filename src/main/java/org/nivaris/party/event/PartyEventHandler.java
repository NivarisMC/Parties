package org.nivaris.party.event;

import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.server.PlayerDisconnectEvent;
import org.allaymc.api.eventbus.event.server.PlayerQuitEvent;
import org.allaymc.api.player.Player;
import org.nivaris.party.Main;
import org.nivaris.party.Party;
import org.nivaris.party.PartyException;
import org.nivaris.proxythread.ProxyThread;
import org.nivaris.proxythread.event.ProxyReceiveDataEvent;

import java.util.Map;

public class PartyEventHandler {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try {
            Player player = event.getPlayer();
            Main.getInstance().getPartyManager().leaveParty(player);
            Main.getInstance().getInvitationManager().removeInvitation(player);
        } catch (PartyException ignored) {
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        try {
            Player player = event.getPlayer();
            Main.getInstance().getInvitationManager().removeInviter(player);
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onProxyReceiveData(ProxyReceiveDataEvent event) {
        var iterator = event.getIterator();
        String identify = null;
        Map<String, Object> data = null;
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (ProxyThread.KEY_IDENTIFY.equals(entry.getKey())) {
                identify = (String) entry.getValue();
            } else if (ProxyThread.KEY_DATA.equals(entry.getKey())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> d = (Map<String, Object>) entry.getValue();
                data = d;
            }
        }

        if (Party.PARTY_PROXY_KEY.equals(identify) && data != null) {
            Main.getInstance().getPartyManager().autoCreatedParty(data);
        }
    }
}
