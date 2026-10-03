package dev.sn.core;

/**
 * Build metadata. The version is read from the mod's own metadata file at runtime, so a single
 * place decides what the {@code /styled-nicknames} output shows.
 */
public final class ModInfo {
    public static final String VERSION = "1.12.1";

    private ModInfo() {
    }
}