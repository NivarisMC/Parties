package org.nivaris.party;

import lombok.Getter;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.api.utils.config.Config;
import org.nivaris.party.command.PartyCommand;
import org.nivaris.party.event.PartyEventHandler;
import org.nivaris.proxythread.MultiProxy;
import org.nivaris.proxythread.ProxyThread;
import org.nivaris.proxythread.libProxyThread;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Main extends Plugin {

    public static final String PROXY_NAME = "party-proxy";

    private static Main instance;

    private PartyTransferManager transferManager;
    private PartyManager partyManager;
    private InvitationManager invitationManager;

    private MultiProxy proxy;
    private final Map<String, Integer> servers = new HashMap<>();
    private Config config;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        loadConfig();
        Messages.init(getPluginContainer().dataFolder().toFile());
        loadServers();
        initializeProxy();
        setupScheduler();
        this.transferManager = new PartyTransferManager();
        this.partyManager = new PartyManager();
        this.invitationManager = new InvitationManager();

        Registries.COMMANDS.register(new PartyCommand());
        Server.getInstance().getEventBus().registerListener(new PartyEventHandler());
    }

    @Override
    public void onDisable() {
        if (proxy != null) {
            proxy.close();
        }
    }

    private void loadConfig() {
        File dataFolder = getPluginContainer().dataFolder().toFile();
        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            dataFolder.mkdirs();
            try (var in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                }
            } catch (Exception e) {
                getPluginLogger().error("Failed to save default config", e);
            }
        }
        this.config = new Config(configFile.toString(), Config.YAML);
    }

    private void loadServers() {
        var serverList = config.getSection("server-list");
        for (var entry : serverList.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> data) {
                Object port = data.get("socket-port");
                if (port instanceof Number) {
                    servers.put(entry.getKey(), ((Number) port).intValue());
                }
            }
        }
    }

    public String getCurrentServer() throws PartyException {
        String server = config.getString("current-server");
        if (server == null || server.isEmpty()) {
            throw new PartyException("key current-server in config is empty or not found");
        }
        return server;
    }

    public int getCurrentProxyPort() throws PartyException {
        String server = getCurrentServer();
        Integer port = servers.get(server);
        if (port == null) {
            throw new PartyException("key " + server + " not found in server-list config");
        }
        return port;
    }

    public Integer getServerProxyPort(String serverName) {
        return servers.get(serverName);
    }

    private void initializeProxy() {
        this.proxy = libProxyThread.createMultiProxy(this, "127.0.0.1");
        try {
            proxy.insert(PROXY_NAME, new ProxyThread(getCurrentProxyPort(), List.copyOf(servers.values())));
            getPluginLogger().info("This server connected to party socket with port " + getCurrentProxyPort());
        } catch (PartyException e) {
            getPluginLogger().error(e.getMessage());
        }
    }

    private void setupScheduler() {
        Server.getInstance().getScheduler().scheduleRepeating(this, () -> {
            var iterator = transferManager.getQueues().iterator();
            while (iterator.hasNext()) {
                var queue = iterator.next();
                if (queue.run()) {
                    iterator.remove();
                }
            }
        }, 1);
    }
}
