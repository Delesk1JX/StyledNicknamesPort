package dev.sn.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes the JSON config file.
 *
 * <p>Writing happens on every load so that a config written by an older version gains the options
 * added since, and so that a missing file is created with the defaults.
 */
public final class ConfigManager {
    public static final String FILE_NAME = "styled-nicknames.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .create();

    private static volatile Config config;

    private ConfigManager() {
    }

    public static Config get() {
        var current = config;

        if (current == null) {
            // Reading a command or a nickname must never fail just because the config was not loaded
            // yet, for example when a mixin runs during world load.
            load(Path.of("config", FILE_NAME));
            current = config;
        }

        return current;
    }

    public static boolean isLoaded() {
        return config != null;
    }

    /**
     * @return true when the file was read or written successfully
     */
    public static boolean load(Path configDir) {
        var file = configDir.resolve(FILE_NAME);

        try {
            ConfigData data;

            if (Files.exists(file)) {
                var json = Files.readString(file, StandardCharsets.UTF_8);
                data = ConfigData.migrate(GSON.fromJson(json, ConfigData.class));
            } else {
                data = new ConfigData();
            }

            Files.createDirectories(configDir);
            Files.writeString(file, GSON.toJson(data), StandardCharsets.UTF_8);

            config = new Config(data);
            return true;
        } catch (IOException | RuntimeException e) {
            CoreMod.LOGGER.error("Could not read {}, falling back to defaults", file, e);

            config = new Config(new ConfigData());
            return false;
        }
    }

    public static Path configDir(String loaderName) {
        return switch (loaderName) {
            case "forge" -> Path.of("config");
            case "neoforge" -> Path.of("config");
            default -> Path.of("config");
        };
    }
}