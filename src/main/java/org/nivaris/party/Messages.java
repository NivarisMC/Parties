package org.nivaris.party;

import org.allaymc.api.utils.config.Config;

import java.io.File;
import java.nio.file.Files;

public class Messages {

    private static Config config;
    private static boolean loaded;

    public static void init(File dataFolder) {
        File messagesFile = new File(dataFolder, "messages.yml");
        if (!messagesFile.exists()) {
            dataFolder.mkdirs();
            try (var in = Messages.class.getClassLoader().getResourceAsStream("messages.yml")) {
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                }
            } catch (Exception e) {
                Main.getInstance().getPluginLogger().error("Failed to save default messages.yml", e);
            }
        }
        config = new Config(messagesFile.toString(), Config.YAML);
        loaded = true;
    }

    public static String get(String key) {
        if (!loaded || config == null) return "§c[MISSING: " + key + "]";
        return config.getString(key, "§c[MISSING: " + key + "]");
    }

    public static String format(String key, Object... args) {
        String message = get(key);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return message;
    }
}
