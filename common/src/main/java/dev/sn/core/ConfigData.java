package dev.sn.core;

import dev.sn.text.ChatColors;
import dev.sn.text.TagRegistry;
import dev.sn.text.TagLikeParser;
import dev.sn.text.TextNode;

import java.util.HashMap;
import java.util.Map;

/**
 * The raw JSON shape of the config. Kept free of parsing logic so that an unknown or missing field
 * simply keeps its default.
 */
public class ConfigData {
    public static final int CURRENT_VERSION = 2;

    public int CONFIG_VERSION_DONT_TOUCH_THIS = CURRENT_VERSION;
    public String _comment = "See https://github.com/Patbox/StyledNicknames#configuration";

    /** Whether every player may change their own nickname without being an operator. */
    public boolean allowByDefault = true;

    /** How a nickname is wrapped when it replaces a player's name. */
    public String nicknameFormat = "#${nickname}";

    /** Used instead of {@link #nicknameFormat} when the nickname only adds colour. */
    public String nicknameFormatColor = "${nickname}";

    /** Maximum length of the rendered nickname; 0 disables the check. */
    public int maxLength = 48;

    public boolean changeDisplayName = true;
    public boolean changePlayerListName = false;

    /** Enables the old {@code &c} style codes. */
    public boolean allowLegacyFormatting = false;

    public String nicknameChangedMessage = "Your nickname has been changed to ${nickname}";
    public String nicknameResetMessage = "Your nickname has been removed!";
    public String tooLongMessage = "This nickname is too long!";
    public boolean allowSpacesInNicknames = false;
    public String nicknameCantContainSpacesMessage = "Nickname can't contain spaces!";

    /** Which formatting tags players may use, keyed by tag name. */
    public Map<String, Boolean> defaultEnabledFormatting = defaults();

    private static Map<String, Boolean> defaults() {
        var map = new HashMap<String, Boolean>();

        for (var tag : TagRegistry.safe().getTags()) {
            // Every colour but plain black, and every formatting flag, is enabled out of the box.
            map.put(tag.name(), tag.type().equals("color") ? !ChatColors.isBlack(tag.name()) : true);
        }

        return map;
    }

    /**
     * Adds options introduced by a newer version and drops ones that no longer exist, so that the
     * written file always matches what the code expects.
     */
    public static ConfigData migrate(ConfigData data) {
        if (data == null) {
            return new ConfigData();
        }

        var defaults = defaults();
        var enabled = data.defaultEnabledFormatting;

        if (enabled == null) {
            data.defaultEnabledFormatting = defaults;
            return data;
        }

        defaults.forEach(enabled::putIfAbsent);
        enabled.keySet().removeIf(key -> !defaults.containsKey(key));

        data.CONFIG_VERSION_DONT_TOUCH_THIS = CURRENT_VERSION;
        return data;
    }
}